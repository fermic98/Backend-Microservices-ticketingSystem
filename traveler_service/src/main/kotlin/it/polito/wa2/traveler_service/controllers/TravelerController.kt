package it.polito.wa2.traveler_service.controllers


import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.crypto.RSADecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jwt.EncryptedJWT
import com.nimbusds.jwt.JWTClaimsSet
import it.polito.wa2.traveler_service.dtos.*
import it.polito.wa2.traveler_service.exceptions.BadRequestException
import it.polito.wa2.traveler_service.services.impl.AdminReportsServiceImpl
import it.polito.wa2.traveler_service.services.impl.TransitServiceImpl
import it.polito.wa2.traveler_service.services.impl.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.*
import javax.validation.Valid


@RestController
@RequestMapping("/traveler")
class TravelerController {

    @Autowired
    lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    lateinit var transitService: TransitServiceImpl

    @Autowired
    lateinit var adminReportsService: AdminReportsServiceImpl

    @Value("\${application.jwt.jwtSecretTicket}")
    private lateinit var jwtSecretTicket: String

    @GetMapping("/my/profile")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).CUSTOMER)")
    @ResponseBody
    fun getMyProfile(): GetMyProfileResponseBody {

        val userName = SecurityContextHolder.getContext().authentication.name
        val userDetailsDTO = userDetailsService.getUserProfile(userName)

        return GetMyProfileResponseBody(
            userDetailsDTO.name,
            userDetailsDTO.address,
            userDetailsDTO.telephone_number,
            userDetailsDTO.date_of_birth
        )
    }

    @PutMapping("/my/profile")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).CUSTOMER)")
    @ResponseBody
    fun putMyProfile(
        @RequestBody @Valid userDetailsDTO: UserDetailsDTO,
        bindingResult: BindingResult
    ) {
        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        //if date not null and not empty , the only valid format is dd-MM-yyyy
        if ((userDetailsDTO.date_of_birth != null && userDetailsDTO.date_of_birth != "") && !validDate(userDetailsDTO.date_of_birth as String))
            throw BadRequestException("Wrong json date field")


        //adding username to the DTO
        userDetailsDTO.username = SecurityContextHolder.getContext().authentication.name
        //putting user info in the db
        userDetailsService.putUserProfile(userDetailsDTO)

    }

    @GetMapping("/my/tickets")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).CUSTOMER)")
    @ResponseBody
    fun getMyTickets(): List<TicketAcquiredDTO> {
        return userDetailsService.getUserTickets(SecurityContextHolder.getContext().authentication.name)
    }
/*
    @PostMapping("/my/tickets")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).SERVICE)")
    @ResponseBody
    fun postMyTickets(
        @RequestBody @Valid purchaseTicketDTO: PurchaseTicketDTO,
        bindingResult: BindingResult
    )/*: List<TicketAcquiredDTO> */{
        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        println(purchaseTicketDTO)

        //posting tickets in the db
        userDetailsService.postUserTickets(
            SecurityContextHolder.getContext().authentication.name,
            purchaseTicketDTO
        )
    }*/

    @GetMapping(path = ["my/tickets/{ticketId}"], produces = [MediaType.IMAGE_PNG_VALUE])
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).CUSTOMER)")
    fun generateQRCodeImage(
        @PathVariable("ticketId") ticketId: String
    ): ByteArray? {

        try {
            val userName = SecurityContextHolder.getContext().authentication.name

            println(ticketId)

            //QRcode generator logic
            val qrCodeWriter = QRCodeWriter()
            val ticketDTO = userDetailsService.getTicketById(ticketId.toLong(), userName)

            if (ticketDTO == null)
                throw BadRequestException("Invalid ticketId")

            val bitMatrix: BitMatrix = qrCodeWriter.encode(ticketDTO.jws, BarcodeFormat.QR_CODE, 250, 250)

            val pngOutputStream = ByteArrayOutputStream()
            val con = MatrixToImageConfig(-0xfffffe, -0x3fbf)

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, con)
            val pngData: ByteArray = pngOutputStream.toByteArray()
            return pngData
        }catch(ex: Exception) {
            throw BadRequestException("Invalid ticketId")
        }


    }

    @GetMapping("/admin/travelers")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getAdminTravelers(): List<String> {
        return userDetailsService.getTravelers()
    }

    @GetMapping("/admin/traveler/{userID}/profile")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getAdminProfile(@PathVariable("userID") userID: String): UserDetailsDTO {
        return userDetailsService.getUserProfile(userID)
    }

    @GetMapping("/admin/traveler/{userID}/tickets")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getAdminTickets(@PathVariable("userID") userID: String): List<TicketAcquiredDTO> {
        return userDetailsService.getUserTickets(userID)
    }



    /** APIs for statistics about purchases and transits  **/

    @GetMapping("/admin/report/{userID}/purchases")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getPurchasesByUser(@PathVariable("userID") userID: String,
                           @RequestBody @Valid dateRangeDTO: DateRangeDTO,
                           bindingResult: BindingResult): List<TicketAcquiredDTO> {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")
        println(dateRangeDTO)
        return adminReportsService.getTicketsAcquiredByUser(userID, dateRangeDTO)
    }

    @GetMapping("/admin/report/{userID}/transits")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getTransitsByUser(@PathVariable("userID") userID: String,
                          @RequestBody @Valid dateTimeRangeDTO: DateTimeRangeDTO,
                          bindingResult: BindingResult): List<TransitDTO> {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")


        return adminReportsService.getTransitsByUser(userID, dateTimeRangeDTO)
    }

    @GetMapping("/admin/report/purchases")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getPurchases( @RequestBody @Valid dateRangeDTO: DateRangeDTO,
                      bindingResult: BindingResult): List<TicketAcquiredDTO> {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")


        return adminReportsService.getTicketsAcquired(dateRangeDTO)
    }
    @GetMapping("/admin/report/transits")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).ADMIN)")
    @ResponseBody
    fun getTransits( @RequestBody @Valid dateTimeRangeDTO: DateTimeRangeDTO,
                     bindingResult: BindingResult): List<TransitDTO> {

        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")


        return adminReportsService.getTransits(dateTimeRangeDTO)
    }


    @PostMapping("embedded_system/transit")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).EMBEDDED_SYSTEM)")
    @ResponseBody
    fun postTransit(
        @RequestBody @Valid transitDTO: TransitDTO,
        bindingResult: BindingResult
    ) {
        if (bindingResult.hasErrors())
            throw BadRequestException("Wrong json fields")

        //putting transit info in the db
        transitService.postTransit(transitDTO)
    }

    @GetMapping("/embedded/secret")
    @PreAuthorize("hasAuthority(T(it.polito.wa2.traveler_service.dtos.Role).EMBEDDED_SYSTEM)")
    @ResponseBody
    fun getSecret(): String {

//        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
//        //initialize key size
//        keyPairGenerator.initialize(2048)
//        //generate the key pair
//        val keyPair = keyPairGenerator.genKeyPair()
//
//        //create key factory and RSA key spec
//        val keyFactory = KeyFactory.getInstance("RSA")
//        val publicKeySpec = keyFactory.getKeySpec(keyPair.public, RSAPublicKeySpec::class.java)
//        val privateKeySpec = keyFactory.getKeySpec(keyPair.private, RSAPrivateKeySpec::class.java)
//
//        //generate RSA keys from the keyfactory using key speccs
//        val publicRsaKey = keyFactory.generatePublic(publicKeySpec) as RSAPublicKey
//        val privateRsaKey = keyFactory.generatePrivate(privateKeySpec) as RSAPrivateKey

        //get public key from file
        val publicKeyFile = File("traveler_service/src/main/resources/rsa.public")
        val publicRsaKey = getPublicKey(publicKeyFile) as RSAPublicKey


        //generate jwt claims
        val claimSet = JWTClaimsSet.Builder().claim("secret", jwtSecretTicket)
        val header = JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM)
        var jwt = EncryptedJWT(header, claimSet.build())

        //Create an RSA encrypted with the specified public RSA key
        val encrypter = RSAEncrypter(publicRsaKey)
        jwt.encrypt(encrypter)

        //decrypt jwt string value to check if Turnstail can correctly decrypt
        jwt = EncryptedJWT.parse(jwt.serialize())
//        val decrypter = RSADecrypter(privateRsaKey)
//        println(jwt.decrypt(decrypter))

        return  jwt.serialize()

    }

    //the only valid format is dd-MM-yyyy
    private fun validDate(date: String): Boolean {
        val stringParsed = date.split("-")
        val day = stringParsed.get(0).toInt()
        val month = stringParsed.get(1).toInt()
        val year = stringParsed.get(2).substring(2, 4).toInt()
        val firstCheck = when (month) {
            //if February
            2 -> {
                if (year % 4 == 0) {//se bisestile (if leap)
                    if (day > 29)
                        false
                    else true
                } else {
                    if (day > 28)
                        false
                    else true
                }

            }
            //if April
            4 -> {
                if (day > 30)
                    false
                else true
            }
            //if June
            6 -> {
                if (day > 30)
                    false
                else true
            }
            //if September
            9 -> {
                if (day > 30)
                    false
                else true
            }
            //if November
            11 -> {
                if (day > 30)
                    false
                else true
            }
            else -> true
        }
        if (firstCheck && notFutureDate(date))
            return true
        else return false
    }

    private fun notFutureDate(date: String): Boolean {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val date = formatter.parse(date)

        if (date.compareTo(formatter.parse(formatter.format(Date()))) <= 0)
            return true
        else return false
    }

    private fun getPublicKey(file: File): PublicKey {
        var bytes: ByteArray = getKeyFromFile(file)

        bytes = Base64.getDecoder().decode(bytes)
        val spec = X509EncodedKeySpec(bytes)
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePublic(spec)
    }

    private fun getKeyFromFile(file: File): ByteArray {
        var fileContent = file.readText()

        // Convert key file to string of Base64 characters:
        // exclusion of "-----BEGIN/END ... KEY-----"
        // exclusion of newlines

        val re = Regex("-----[^-]*-----")
        fileContent = re.replace(fileContent, "")
        fileContent = fileContent.replace("\r", "").replace("\n", "")

        return fileContent.toByteArray()
    }

}

data class GetMyProfileResponseBody(
    val name: String?,
    val address: String?,
    val telephone_number: String?,
    val date_of_birth: String?
) {}
