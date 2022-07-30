package gay.lemmaeof.kdlycontent.api;

import net.minecraft.util.Identifier;

public class ParseException extends RuntimeException {
	public ParseException(Identifier id, String message) {
		super("Error parsing KDL for " + id.toString() + ": " + message);
	}
}
