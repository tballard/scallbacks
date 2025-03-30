package sweet

import cats.Id
import chameleon.ext.upickle.*
import chameleon.{Deserializer, Serializer}
import sloth.{ClientHandler, RequestTransport, ServerFailure}
import upickle.default.{read, write, *}

/**
 * This is a demo of how to make the callbacks defined in your 
 * scalatags-generated (or similar) HTML pages can be strongly typed.
 * (Really!)
 * 
 * This demo shows how you can get a String holding what is needed to
 * run a command and then how to actually run it.
 *
 * This uses the "sloth" RPC library in a slightly unconventional manner.
 */

/** Here is the API definition that is shared between client and server */
trait ExpShareCbApi {
  def times(i: Int, j: Int): Id[String]
}

/** Unlike a conventional callback, the above method returns a
 * string because this is a bit of a hack. This is a decent return
 * because it might lead you here.
 */
def END: String = null

/**
 * This is the actual callback implementation on the client. 
 * It implements the API. Methods return String because of 
 * the hackish nature
 */
object ExpClientCbImpl extends ExpShareCbApi {
  def times(i: Int, j: Int): Id[String] = {
    println("Running remote trySomething. YAY")
    println(i * j)
    END
  }
}

/**
 * On the client you need a router to handle the callbacks. All callbacks
 * go through it. In scala.js, you would expose it to Javascript with
 * JSExport, JSExportTopLevel
 */
object ClientCbHandler extends RpcImplicits {

  /**
   * This is where you register the Implementation and the
   * implementation it is implementing.
   */
  val routerCo: sloth.RouterCo[String, Id] =
    sloth.Router[String, Id].
      route[ExpShareCbApi](ExpClientCbImpl);

  /**
   * This is how the request held in the onclick or whatever actually
   * gets called. Sloth does its magic to look up the method call 
   * being requested and call it.
   * @param request
   */
  def handle(request: String): Unit =
    routerCo(read[sloth.Request[String]](request)) match {
      case Right(resultId: Id[String]) =>
      case Left(err) =>
        println(err.toException.failure.toException.getMessage)
    }
}

/**
 * This is on the server. 
 */
object ServerSubmitter {
  /**
   * When you want to create callbacks, this is where sloth creates
   * the server-side implementation of the API that you can call
   * to create callbacks.
   */
  private val remote: ExpShareCbApi = ServerCallback.wire[ExpShareCbApi]()

  /**
   * This demonstrates how you get the string that represents the 
   * callback you might want to call on the client.
   */
  def main(args: Array[String]): Unit =
    val yerPackedCmd = remote.times(10, 33)
    // Normally you use yerPackedCmd to create a callback in your
    // html file. This just directly calls the method that javascript
    // would call.
    ClientCbHandler.handle(yerPackedCmd)
}

/** 
 * This is the server side code that uses sloth in a slightly hackish
 * manner to do what we want done.
 */
object ServerCallback extends RpcImplicits {

  /**
   * Normally with sloth you have to implement your transport between client
   * and server. Here we just want to create the message and short circuit the 
   * process. Instead of a Future we return cats.Id which is basically 
   * a String that suits RequestTransport.
   */
  private object CallbackTransport extends RequestTransport[String, Id] {

    /**
     * @param request The sloth request object to "transport"
     * @return The json expression we want to send, quoted and 
     *         wrapped as a string
     */
    override def apply(request: sloth.Request[String]): Id[String] =
      s"\"${write(request).replace("\\", "\\\\").replace("\"", "\\\"")}\""
  }

  /**
   * Sloth requires a `ClientHandler[Id]` to work with synchronous calls.
   * It is on the server since it is the "client" in this case.
   * This normally is instrumental in handling errors. Since our 
   * transport is through javascript, good luck with that. Just assume
   * success.
   */
  private implicit val clientHandler: ClientHandler[Id] = new ClientHandler[Id] {
    def eitherMap[A, B](fa: Id[A])
                       (f: A => Either[sloth.ClientFailure, B]): Id[B] =
      f(fa) match {
        case Right(b) => b
        case Left(error) => throw new RuntimeException(s"Client error: $error")
      }

    /**
     * Fail ignominiously
     */
    def raiseFailure[B](failure: sloth.ClientFailure): Id[B] =
      throw new RuntimeException("Your callback magic isn't")
  }

  /** This is what handles it all, using wire() */
  private val rpcClient = sloth.Client[String, Id](CallbackTransport)

  /**
   * This is what you call with your api trait to wire it up
   * as demonstrated above
   */
  inline def wire[T](): T = rpcClient.wire[T]
}

/**
 * This is needed for upickle serialization. You may serialize as you please.
 */
trait RpcImplicits:
  implicit val methodRw: ReadWriter[sloth.Method] = macroRW[sloth.Method]
  implicit val requestRw: ReadWriter[sloth.Request[String]] = macroRW[sloth.Request[String]]

