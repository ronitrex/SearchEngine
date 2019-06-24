package modules.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;

/**
 * A AdvancedTokenProcessor creates terms from tokens by following the below
 * mentioned rules: 1. Removing all alpha-numeric characters from the beginning
 * and end of the token, but not the middle. 2. Remove all apostrophes or
 * quotation marks from anywhere in the string 3. Hyphen rule specified in the
 * documentation 4. Convert to lowercase.
 */

public class AdvancedTokenProcessor implements TokenProcessor {
	HashSet<Character> accentedCharacters = new HashSet<Character>();
	@Override
	public List<String> processToken(String token) {
		List<String> processedTokens = new ArrayList<String>();
		String stemmedToken = "";
		// Rule 1 & 4
		token = token.replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "").toLowerCase();

		// Rule 2
		if (token.contains("\""))
			token = token.replace("\"", "");
		if (token.contains("'"))
			token = token.replace("\'", "");
		try {
			stemmedToken = stemTokenJava(token);
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// To handle accented tokens
		/*String unaccentedToken = "";
		try {
			unaccentedToken = StringUtils.stripAccents(token);
		} catch (Exception e) {
		}
		if (!unaccentedToken.equals(token)) { // unaccented token not equal to token, means it contains accent
			processedTokens.add(token);
			processedTokens.add(unaccentedToken);
		} else { // no accent process as normal English token

			try {
				token = stemTokenJava(token);
			} catch (Throwable e) {
				e.printStackTrace();
			}*/
		
			// Rule 3
			if (token.contains("-") && !token.startsWith("-")) {
				String[] parts = token.split("-");
				String combinedToken = "";
				for (String s : parts) {
					processedTokens.add(s);
					combinedToken += s;
				}
				processedTokens.add(combinedToken);
			} else {
				processedTokens.add(stemmedToken);
			}
		return processedTokens;
	}

	public static String stemTokenJava(String token) throws Throwable {
		Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
		SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
		stemmer.setCurrent(token);
		stemmer.stem();
		String stemmedToken = stemmer.getCurrent();
		return stemmedToken;
	}
}
