package di.vdrchman;

public class ArgsParser {

	private Command command;
	private Integer userId;
	private String sourceName;

	public void parse(String[] args) {
		String argCommand;

		if (args.length > 1) {
			argCommand = args[0];
			userId = Integer.parseInt(args[1]);

			if (args.length == 2) {
				if (argCommand.equals("cleanDb")) {
					command = Command.CLEAN_DB;
				}
				if (argCommand.equals("loadSources")) {
					command = Command.LOAD_SOURCES;
				}
			} else {
				sourceName = args[2];

				if (args.length == 3) {
					if (argCommand.equals("loadTransponders")) {
						command = Command.LOAD_TRANSPONDERS;
					}
				}
			}
		}

	}

	public Command getCommand() {
		return command;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getSourceName() {
		return sourceName;
	}

}
