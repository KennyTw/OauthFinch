package com.e104

import java.util.Date

import com.twitter.finagle.httpx.{Request, Status, Version, Response}
import com.twitter.finagle.httpx.path._
import com.twitter.finagle.httpx.service.RoutingService
import com.twitter.finagle.oauth2._
import com.twitter.finagle._
import com.twitter.util.{Await, Future}
import io.finch.request.{RequestReader,_}
import io.finch.route.{Router, get, _}
import io.finch.response.{TurnIntoHttp, Ok}

/**
 * Created by kenny.lee on 2015/10/17.
 */

/**
 * Oauth User Data class
 */
case class User(id: Long, name: String)

/**
 * Implement DataHandler with User class
 */
class dataHandlerImp extends DataHandler[User] {
  def validateClient(clientId: String, clientSecret: String, grantType: String): Future[Boolean] = {
    println("validateClient")
    Future.value(true) }

  def findClientUser(clientId: String, clientSecret: String, scope: Option[String]): Future[Option[User]] =  {
    println("findClientUser")
    Future.value(Some(User(10000, "username"))) }

  def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
    println("createAccessToken")
    Future.value(AccessToken("", Some(""), Some(""), Some(0L), new Date()))
  }

  def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = {
    println("refreshAccessToken")
    Future.value(AccessToken("", Some(""), Some(""), Some(0L), new Date()))
  }

  def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = {
    println("findAuthInfoByRefreshToken")
    Future.value(None)
  }

  def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = {
    println("getStoredAccessToken")
    Future.value(None)
  }

  def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] =  {
    println("findAuthInfoByAccessToken:" + accessToken.token)
    Future.value(Some(AuthInfo(user = User(10000, "username"), clientId = "clientId1", scope = Some("all"), redirectUri = None)))
  }

  def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] =  {
    println("findAuthInfoByCode:" + code)
    Future.value(Some(AuthInfo(user = User(10000, "username"), clientId = "clientId1", scope = Some("all"), redirectUri = None)))
  }

  def findUser(username: String, password: String): Future[Option[User]] = {
    println("findUser")
    Future.value(None)
  }

  def findAccessToken(token: String): Future[Option[AccessToken]] = {
    println("findAccessToken:" + token)
    Future.value(Some(AccessToken("token1", Some("refreshToken1"), Some("all"), Some(3600), new Date())))
  }
}


object Oauth  extends App  {
  val dataHandler = new dataHandlerImp()
  // accessing a protected resource via finagled filter
  val auth = new OAuth2Filter(dataHandler) with OAuthErrorInJson

  // a protected resource example
  /*val hello = new Service[OAuth2Request[User], Response] {
    def apply(req: OAuth2Request[User]) = {
      val rtn = s"Hello, ${req.authInfo.user}!"
      println(rtn)

      val response = Response()
      response.setContentString("Hello from Finagle\n")

      Future.value(response)
    }
  }*/

  /*val backend = RoutingService.byPathObject {
    case Root / "hello" => auth andThen hello
  }*/

  /**
   * finch Router
   * */
  val api1: Router[Response] = get("one") {
    Ok("foo") }

  val api5 : Router[String] =
    post("data" ? urldata) {(urldata: (String, String, Int)) => s"Hello " + urldata._1 + "," + urldata._2 + "," + urldata._3}

  /**
   * Finch Request Reader
   * */
  val urldata: RequestReader[(String, String, Int)] =
    (paramOption("x").as[String] :: paramOption("y").as[String] :: paramOption("z").as[Int]).asTuple.map {
      case (x, y, z) => (x.getOrElse(""), y.getOrElse(""),z.getOrElse(0))
    }

  /**
   * finch router compose
   * */
  val allapi = api1 :+: api5

  /**
   * Convert finch service to OAuth2Request
   * */
  def toAuth(from : Service[Request, Response]) : Service[OAuth2Request[User], Response] = {
    new Service[OAuth2Request[User], Response] {
      def apply(req: OAuth2Request[User]) = {
        from.apply(req.httpRequest)

        //val rtn = s"Hello, ${req.authInfo.user}!"
       // println(rtn)
        //val response = Response()
       // response.setContentString("Hello from Finagle\n")
        //Future.value(response)
      }
    }

  }

  val backend =  auth andThen toAuth(allapi.toService)
  val server = Httpx.serve(":8080",backend )
  Await.ready(server)

}
