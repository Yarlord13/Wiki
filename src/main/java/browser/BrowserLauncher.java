package browser;

import java.awt.Desktop;
import java.net.URI;

public final class BrowserLauncher {

    private static final String ARTICLE_URL =
            "https://ru.wikipedia.org/w/index.php?curid=";

    private BrowserLauncher() {}

    public static boolean open(long pageId) {
        String url = ARTICLE_URL + pageId;

        try {
            if (!Desktop.isDesktopSupported()) {
                reportFallback(url, "Desktop API не поддерживается");
                return false;
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                reportFallback(url, "открытие браузера не поддерживается");
                return false;
            }
            desktop.browse(URI.create(url));
            return true;

        } catch (Exception e) {
            reportFallback(url, e.getMessage());
            return false;
        }
    }

    private static void reportFallback(String url, String reason) {
        System.out.println("Не удалось открыть браузер: " + reason);
        System.out.println("Откройте вручную: " + url);
    }
}