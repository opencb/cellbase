package org.opencb.cellbase.app.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

@Deprecated
public class OptionFactory {

	public static Option createOption(String name, String description) {
		return createOption(name, description, true, true, ' ');
	}
	
	public static Option createOption(String name, String shortName, String description) {
		return createOption(name, shortName, description, true, true, ' ');
	}
	
	public static Option createOption(String name, String description, boolean required) {
		return createOption(name, description, required, true, ' ');
	}
	
	public static Option createOption(String name, String shortName, String description, boolean required) {
		return createOption(name, shortName, description, required, true, ' ');
	}
	
	public static Option createOption(String name, String description, boolean required, boolean hasArgs) {
		return createOption(name, description, required, hasArgs, ' ');
	}
	
	public static Option createOption(String name, String shortName,  String description, boolean required, boolean hasArgs) {
		return createOption(name, shortName, description, required, hasArgs, ' ');
	}
	
	public static Option createOption(String name, String description, boolean required, boolean hasArgs, char valueSeparator) {
		OptionBuilder.withLongOpt(name);
		OptionBuilder.withDescription(description);
		OptionBuilder.withValueSeparator(valueSeparator);
		OptionBuilder.isRequired(required);
		OptionBuilder.hasArg(hasArgs);
		return OptionBuilder.create();
	}
	
	public static Option createOption(String name, String shortName, String description, boolean required, boolean hasArgs, char valueSeparator) {
		OptionBuilder.withLongOpt(name);
		OptionBuilder.withDescription(description);
		OptionBuilder.withValueSeparator(valueSeparator);
		OptionBuilder.isRequired(required);
		OptionBuilder.hasArg(hasArgs);
		return OptionBuilder.create(shortName);
	}
}
