
class logUtils implements Serializable {
    static private boolean debugMode;
    static pp;

    def init(who){
	if (who.params.containsKey("pp_debug")) {
	    debugMode=who.params.pp_debug;
	} else debugMode = false;
	pp = who;
    }

    def getDebugMode() {
	debugMode	
    }
    def trace(msg){
        if (debugMode) pp.echo "--debug: " + msg;	 
    }
}

