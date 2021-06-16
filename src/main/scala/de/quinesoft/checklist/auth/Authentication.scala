package de.quinesoft.checklist.auth

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import de.quinesoft.checklist.model.Login
import io.circe.Json
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import java.security.{KeyPairGenerator, SecureRandom, Security}
import java.security.spec.ECGenParameterSpec
import scala.util.{Failure, Success}

trait Authentication {
  def verifyUser: Login => Boolean = {}

  def setClaims(user: String): Json =
    Json.obj(
      ("user", Json.fromString(user)),
      ("role", Json.fromString("you can store anything"))
    )

  val ecKey = {
    val ecGenSpec = new ECGenParameterSpec("P-521")

    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider())
    }

    val generatorEC = KeyPairGenerator.getInstance("ECDSA", "BC")

    generatorEC.initialize(ecGenSpec, new SecureRandom())
    generatorEC.generateKeyPair()
  }
  private val algo = JwtAlgorithm.ES256

  def getJwtToken(claims: Json): String = {
    JwtCirce.encode(claims, ecKey.getPrivate, algo)
  }

  private def getClaims(jwt: String): Map[String, String] = {
    JwtCirce
      .decodeJson(jwt, ecKey.getPublic, Seq(algo)) match {
      case Failure(_)     => Map.empty
      case Success(value) => value.as[Map[String, String]].getOrElse(Map.empty)
    }
  }

  def authenticated: Directive1[Map[String, Any]] =
    optionalHeaderValueByName("Access-Token").flatMap {
      case Some(jwt) =>
        val claim = getClaims(jwt)
        if (claim.nonEmpty && claim.contains("user")) {
          provide(claim)
        } else complete(StatusCodes.Unauthorized)
      case _ => complete(StatusCodes.Unauthorized)
    }
}
