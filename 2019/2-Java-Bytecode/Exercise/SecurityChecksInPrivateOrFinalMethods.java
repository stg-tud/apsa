import java.io.File;

class SecurityChecksInPrivateOrFinalMethods {

	public void nonCompliantPublicProcessSensitiveFile() {
		String f = "FileName";
		try {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				sm.checkRead(f);
			}
			// process file
		} catch (SecurityException se) {
			// handle exception
		}
	}

	public final void finalProcessSensitiveFile() {
		String f = "FileName";
		try {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				sm.checkRead(f);
			}
			// process file
		} catch (SecurityException se) {
			// handle exception
		}
	}

	private void privateProcessSensitiveFile() {
		String f = "FileName";
		try {
			SecurityManager sm = System.getSecurityManager();
			if (sm != null) {
				sm.checkRead(f);
			}
			// process file
		} catch (SecurityException se) {
			// handle exception
		}
	}

	public String toString() {
		privateProcessSensitiveFile();
		return "";
	}
}
