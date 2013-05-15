/*
 * Copyright (C) 2009 Dimagi Inc., UNICEF
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

/**
 * 
 */
package org.rapidsms.java.core.parser;
import android.util.Log; 
import java.util.Vector;

import org.rapidsms.java.core.model.Field;
import org.rapidsms.java.core.model.Form;
import org.rapidsms.java.core.parser.token.ITokenParser;

/**
 * @author Daniel Myung dmyung@dimagi.com
 * @created Jan 16, 2009
 * 
 *          The first instance of a message parser for RapidAndroid
 * 
 *          The objective for this parser is to have a simple, greedy order
 *          dependent parse of a message
 * 
 *          for a given message MSG and a form F with fields [a,b,c,d,e]
 * 
 *          where the fields have regexes for each "token" they want to parse
 *          out (height measurement or a string for example)
 * 
 *          This parser will iterate through each field in order, greedily try
 *          to find the *first* instance of the match it can find from its regex
 *          Slice out the substring of the first match from the original message
 *          MSG, and continue onto the next field again.
 */
public class SimpleRegexParser implements IMessageParser {

	public SimpleRegexParser() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.rapidsms.java.core.parser.IMessageParser#CanParse(java.lang.String)
	 */

	public boolean CanParse(String input) {
		// TODO Auto-generated method stub
		return false;
	}

	public Vector<IParseResult> ParseMessage(Form f, String input) {
		// System.out.println("");
		// System.out.println("");
		// System.out.println("********** begin ParseMessage ************");

		// ok, for this iteration, we're going to greedily determine if this is
		// a message we can fracking parse.

		String prefix = f.getPrefix();
		// System.out.println("what's the fracking form prefix: " + prefix);
		input = input.toLowerCase().trim();
		if (input.startsWith(prefix.toLowerCase() + " ")) {
			Log.i("SimpleRegexParsingService", "input started with prefix, input: " + input);
			input = input.substring(prefix.length()).trim();
		} else {
			
			return null;
		}

		Vector<IParseResult> results = new Vector<IParseResult>();
		Field[] fields = f.getFields();
		int length = fields.length;

		for (int i = 0; i < length; i++) {
			Log.i("SimpleRegexParsingService", "field" + i + ": " + fields[i].getName());
			Log.i("SimpleRegexParsingService", "field" + i + ": " + fields[i].getFieldType().getParsedDataType());

			Log.i("SimpleRegexParsingService", "field" + i + ": " + fields[i].getFieldType().getReadableName());
			ITokenParser parser = fields[i].getFieldType();
			// System.out.println("Begin field parse: [" + fields[i].getName() +
			// "] on input: {" + input + "}");
			IParseResult res = parser.Parse(input);

			// ok, so we got the res, so we need to subtract the parsed string
			// if at all possible.
			if (res != null) {
				String justParsedToken = res.getParsedToken();
				int tokLen = justParsedToken.length();
				// System.out.println("Parsed input:" + input);
				// System.out.println("Just parsed:" + justParsedToken + "##");
				int tokStart = input.indexOf(justParsedToken);
				// System.out.println("tokLen: " + tokLen);

				if (tokStart > 0) {
					tokStart = tokStart - 1; // need to shift over one for the
												// shiftage
				}
				// int tokRest = tokStart+1;

				// System.out.println("tokStart: " + tokStart);
				// System.out.println("inputLen: " + input.length());
				String newInput = input.substring(0, tokStart) + input.substring(tokLen);

				input = newInput.trim();
			}
			results.add(res);
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.rapidsms.java.core.parser.IMessageParser#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return "simpleregex";
	}

}
