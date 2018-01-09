
class logUtils implements Serializable {
    private static boolean debugMode;
    static pipeline;

    void init(who) {
        if (who.params.containsKey("pp_debug")) {
            debugMode = who.params.pp_debug;
        } else {
            debugMode = false;
        }
        pipeline = who;
    }

    boolean getDebugMode() {
        return debugMode
    }

    void trace(String msg) {
        if (debugMode) {
            pipeline.echo "--debug: " + msg;
        }
    }
}

