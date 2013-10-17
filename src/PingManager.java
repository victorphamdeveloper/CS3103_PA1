import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class PingManager implements Runnable {
	@Override
	public void run() {

		BufferedWriter w = JabberMain.connection.getWriter();
		while (true) {
			try {
				Thread.sleep(300000);
				w.write(" ");
				w.flush();
			} catch (InterruptedException e) {
				e.printStackTrace();

			} catch (IOException e) {
				JabberMain.reconnect();
				return;
			}

		}

	}

}
