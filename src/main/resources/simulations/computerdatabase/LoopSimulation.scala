package computerdatabase

import akka.util.duration._
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import bootstrap._

class LoopSimulation extends Simulation {

	def apply = {

		val baseURL = "http://computer-database.herokuapp.com"

		val httpConf = httpConfig
						.baseURL(baseURL)


		val scn 
			= scenario("Play with the Computer Database")
				.exec(
					http("Index page")
						.get("/")
						.check(
							css("head title").is("Computers database"),
							currentLocation.is(baseURL + "/computers")
						)
				)
				
				.exec((session: Session) 
						=> session.setAttribute("pageIndex", 0) )
				.asLongAs((s:Session) => !s.isAttributeDefined("ibmSystemZLocation") 
							&& s.getTypedAttribute[Int]("pageIndex") < 3) {
						
						pauseExp(3 seconds)
						.exec(
							http("Apple computers")
								.get("/computers?f=ibm&p=${pageIndex}")
								.check(
									regex("""(?s)<a href="([^"]+)">IBM System z</a>""").whatever.saveAs("ibmSystemZLocation")
							)
						)
						.exec((session: Session) 
							=> session.setAttribute("pageIndex", session.getTypedAttribute[Int]("pageIndex") + 1) ) 
					}
					
				

				.pauseExp(3 seconds)
				.exec(
					http("IBM System z")
						.get("${ibmSystemZLocation}")
						.check(
							css("#name", "value").is("IBM System z")
						)
				)


		List(scn.configure.users(100).ramp(20).protocolConfig(httpConf))
	}
}
