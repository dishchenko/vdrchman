package di.vdrchman;

public class ArgsParser {

	private Command command;
	private Long userId;
	private String userName;
	private String passwordHash;
	private String sourceName;

	// Parse the command line arguments and
	// set member values to the parsed values
	public void parse(String[] args) {
		String argCommand;

		if (args.length > 1) {
			argCommand = args[0];

			if (argCommand.equals("addUser") && (args.length == 3)) {
				userName = args[1];
				passwordHash = args[2];
				command = Command.ADD_USER;
			} else {
				userId = Long.parseLong(args[1]);

				if (args.length == 2) {
					if (argCommand.equals("removeUser")) {
						command = Command.REMOVE_USER;
					}
					if (argCommand.equals("cleanDb")) {
						command = Command.CLEAN_DB;
					}
					if (argCommand.equals("loadSources")) {
						command = Command.LOAD_SOURCES;
					}
					if (argCommand.equals("loadGroups")) {
						command = Command.LOAD_GROUPS;
					}
					if (argCommand.equals("renumberSeqnos")) {
						command = Command.RENUMBER_SEQNOS;
					}
				} else {
					sourceName = args[2];

					if (args.length == 3) {
						if (argCommand.equals("setPassword")) {
							sourceName = null;
							passwordHash = args[2];
							command = Command.SET_PASSWORD;
						}
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
						if (argCommand.equals("loadIgnoredChannels")) {
							command = Command.LOAD_IGNORED_CHANNELS;
						}
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

	public String getUserName() {

		return userName;
	}

	public String getPasswordHash() {

		return passwordHash;
	}

	public String getSourceName() {

		return sourceName;
	}

}
