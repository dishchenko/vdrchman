package di.vdrchman;

public class ArgsParser {

	private Command command;
	private Long userId;
	private String sourceName;

	// Parse the command line arguments and
	// set member values to the parsed values
	public void parse(String[] args) {
		String argCommand;

		if (args.length > 1) {
			argCommand = args[0];
			userId = Long.parseLong(args[1]);

			if (args.length == 2) {
				if (argCommand.equals("cleanDb")) {
					command = Command.CLEAN_DB;
				}
				if (argCommand.equals("loadSources")) {
					command = Command.LOAD_SOURCES;
				}
				if (argCommand.equals("loadGroups")) {
					command = Command.LOAD_GROUPS;
				}
			} else {
				sourceName = args[2];

				if (args.length == 3) {
					if (argCommand.equals("loadTransponders")) {
						command = Command.LOAD_TRANSPONDERS;
					}
					if (argCommand.equals("loadNidsTids")) {
						command = Command.LOAD_NIDS_TIDS;
					}
					if (argCommand.equals("loadChannels")) {
						command = Command.LOAD_CHANNELS;
					}
					if (argCommand.equals("loadChannelGroups")) {
						command = Command.LOAD_CHANNEL_GROUPS;
					}
				}
			}
		}

	}

	public Command getCommand() {

		return command;
	}

	public Long getUserId() {

		return userId;
	}

	public String getSourceName() {

		return sourceName;
	}

}
