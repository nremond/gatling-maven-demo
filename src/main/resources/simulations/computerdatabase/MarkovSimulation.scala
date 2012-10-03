package computerdatabase

import akka.util.duration._
import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import bootstrap._

class MarkovSimulation extends Simulation {

	def apply = {

		val baseURL = "http://computer-database.herokuapp.com"

		val httpConf = httpConfig
						.baseURL(baseURL)


		val browseAppleLisa = 
				exec(
					http("Apple computers")
						.get("/computers?f=apple")
						.check(
							regex("""(?s)<a href="([^"]+)">Apple Lisa</a>""").find.saveAs("appleLisaLocation")
						)
				)
				.pauseExp(2 seconds)
				.exec(
					http("Apple Lisa")
						.get("${appleLisaLocation}")
						.check(
							css("#name", "value").is("Apple Lisa")
						)
				)

		val browseIbms = 
				exec(
					http("IBM computers")
						.get("/computers?f=ibm")
						.check(
							regex("""(?s)<a href="([^"]+)">IBM 305</a>""").find.saveAs("ibm305Location"),
							regex("""(?s)<a href="([^"]+)">IBM 701</a>""").find.saveAs("ibm701Location")
						)
				)
				.pauseExp(2 seconds)
				.randomSwitch(
						54 -> exec(
								http("IBM 305")
									.get("${ibm305Location}")
									.check(
										css("#name", "value").is("IBM 305")
									)
							),
						40 -> exec(
								http("IBM 701")
									.get("${ibm701Location}")
									.check(
										css("#name", "value").is("IBM 701")
									)
							)
					)


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
				.pauseExp(3 seconds)
				.randomSwitch(
					43 -> browseIbms,
					31 -> browseAppleLisa
				)

		List(scn.configure.users(1000).ramp(200).protocolConfig(httpConf))
	}
}
