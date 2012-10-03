package computerdatabase

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._

class BasicSimulation extends Simulation {

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
				.exec(
					http("Apple computers")
						.get("/computers?f=apple")
						.check(
							regex("""(?s)<a href="([^"]+)">Apple Lisa</a>""").find.saveAs("appleLisaLocation")
						)
				)
				.exec(
					http("Apple Lisa")
						.get("${appleLisaLocation}")
						.check(
							css("#name", "value").is("Apple Lisa")
						)
				)

		List(scn.configure.users(10).ramp(60).protocolConfig(httpConf))
	}
}
