package org.nick.wwwjdic.app.kanjivg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nick.wwwjdic.app.server.CacheController;

import com.google.appengine.repackaged.org.json.JSONArray;
import com.google.appengine.repackaged.org.json.JSONException;
import com.google.appengine.repackaged.org.json.JSONObject;

public class KanjiStrokesServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1176775953028953526L;

    private static final Logger log = Logger
            .getLogger(KanjiStrokesServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String xuserAgent = req.getHeader("X-User-Agent");
        if (xuserAgent != null) {
            log.info("X-User-Agent: " + xuserAgent);
        }

        String xDeviceVersion = req.getHeader("X-Device-Version");
        if (xDeviceVersion != null) {
            log.info("X-Device-Version: " + xDeviceVersion);
        }

        boolean useJson = false;
        String format = req.getParameter("f");
        if (format != null && format.equals("json")) {
            useJson = true;
        }
        String unicodeNumber = req.getPathInfo().replace("/", "");
        log.info("got request for " + unicodeNumber);

        if (useJson) {
            String kanji = findKanjiJson(unicodeNumber);

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/x-javascript");

            PrintWriter out = resp.getWriter();
            out.write("var kanjis = ");
            out.write(kanji);
            out.flush();
            out.close();
        } else {
            String kanji = findKanji(unicodeNumber);

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/plain");

            PrintWriter out = resp.getWriter();
            out.write(kanji);
            out.flush();
            out.close();
        }
    }

    @SuppressWarnings("unchecked")
    private String findKanjiJson(String unicodeNumber) {

        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query q = null;

        Transaction tx = null;
        try {
            Kanji k = null;

            Kanji cachedKanji = (Kanji) CacheController.get(unicodeNumber);
            if (cachedKanji != null) {
                k = cachedKanji;
                log.info("Got kanji from cache: " + unicodeNumber);
            } else {
                q = pm.newQuery("select from org.nick.wwwjdic.app.kanjivg.Kanji "
                        + "where unicodeNumber == unicodeNumberParam "
                        + "parameters String unicodeNumberParam ");

                tx = pm.currentTransaction();
                tx.begin();

                List<Kanji> kanjis = (List<Kanji>) q.execute(unicodeNumber);
                if (kanjis.isEmpty()) {
                    log.info(String.format("KanjiVG data for %s not found",
                            unicodeNumber));
                    return String.format("not found (%s)", unicodeNumber);
                }

                k = kanjis.get(0);
                CacheController.put(unicodeNumber, k);
                log.info("Put kanji in cache: " + unicodeNumber);
            }

            try {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("kanji", k.getMidashi());
                jsonObj.put("unicode", k.getUnicodeNumber());

                JSONArray pathsArr = new JSONArray();
                List<Stroke> strokes = k.getStrokes();
                for (Stroke s : strokes) {
                    String path = s.getPath();
                    // log.info("path: " + path);
                    if (!"".equals(path) && !"null".equals(path)
                            && path != null) {
                        pathsArr.put(s.getPath());
                    }
                }
                jsonObj.put("paths", pathsArr);

                JSONArray jsonArr = new JSONArray();
                jsonArr.put(jsonObj);
                return jsonArr.toString();
            } catch (JSONException e) {
                log.severe(e.getMessage());
                throw new RuntimeException(e);
            }

        } finally {
            if (tx != null && tx.isActive()) {
                tx.commit();
            }

            if (q != null) {
                q.closeAll();
            }

            pm.close();
        }
    }

    @SuppressWarnings("unchecked")
    private String findKanji(String unicodeNumber) {

        PersistenceManager pm = PMF.get().getPersistenceManager();
        Query q = null;

        Transaction tx = null;
        try {
            Kanji k = null;

            Kanji cachedKanji = (Kanji) CacheController.get(unicodeNumber);
            if (cachedKanji != null) {
                k = cachedKanji;
                log.info("Got kanji from cache: " + unicodeNumber);
            } else {
                q = pm.newQuery("select from org.nick.wwwjdic.app.kanjivg.Kanji "
                        + "where unicodeNumber == unicodeNumberParam "
                        + "parameters String unicodeNumberParam ");

                tx = pm.currentTransaction();
                tx.begin();

                List<Kanji> kanjis = (List<Kanji>) q.execute(unicodeNumber);
                if (kanjis.isEmpty()) {
                    log.info(String.format("KanjiVG data for %s not found",
                            unicodeNumber));
                    return String.format("not found (%s)", unicodeNumber);
                }

                k = kanjis.get(0);
                CacheController.put(unicodeNumber, k);
                log.info("Put kanji in cache: " + unicodeNumber);
            }
            String result = k.getMidashi() + " " + k.getUnicodeNumber() + "\n";
            log.info("returning " + result);

            List<Stroke> strokes = k.getStrokes();
            for (Stroke s : strokes) {
                String path = s.getPath();
                // log.info("path: " + path);
                if (!"".equals(path) && !"null".equals(path) && path != null) {
                    result += s.getPath();
                    result += "\n";
                }
            }

            return result;

        } finally {
            if (tx != null && tx.isActive()) {
                tx.commit();
            }

            if (q != null) {
                q.closeAll();
            }

            pm.close();
        }
    }

}
