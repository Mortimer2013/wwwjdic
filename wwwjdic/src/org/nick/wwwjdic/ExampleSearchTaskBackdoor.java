package org.nick.wwwjdic;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExampleSearchTaskBackdoor extends
        BackdoorSearchTask<ExampleSentence> {

    private static final Pattern SENTENCE_PATTERN = Pattern
            .compile("^A:\\s(\\S+)\t(.+)#.*$");
    private static final Pattern BREAKDOWN_PATTERN = Pattern
            .compile("^B:\\s.+\\{(\\S+)\\}.*$");
    private static final Pattern FORM_MATCHER = Pattern
            .compile("(\\S+)\\{(\\S+)\\}");

    private ExampleSentence lastSentence;

    public ExampleSearchTaskBackdoor(String url, int timeoutSeconds,
            ResultListView<ExampleSentence> resultView,
            SearchCriteria searchCriteria) {
        super(url, timeoutSeconds, resultView, searchCriteria);
    }

    @Override
    protected ExampleSentence parseEntry(String entryStr) {
        Matcher m = SENTENCE_PATTERN.matcher(entryStr);
        if (m.matches()) {
            String japanese = m.group(1);
            String english = m.group(2);
            ExampleSentence result = new ExampleSentence(japanese, english);
            lastSentence = result;

            return result;
        }

        if (lastSentence == null) {
            return null;
        }

        m = BREAKDOWN_PATTERN.matcher(entryStr);
        if (m.matches()) {
            Matcher formMatcher = FORM_MATCHER.matcher(entryStr);
            while (formMatcher.find()) {
                String queryForm = query.getQueryString();
                String basicForm = formMatcher.group(1);
                String formInSentence = formMatcher.group(2);
                if (queryForm.equals(basicForm)) {
                    lastSentence.addMatch(formInSentence);
                } else if (queryForm.equals(formInSentence)) {
                    lastSentence.addMatch(formInSentence);
                }
            }
        }

        return null;
    }

    @Override
    protected String generateBackdoorCode(SearchCriteria criteria) {
        StringBuffer buff = new StringBuffer();
        // dictionary code always 1 for examples?
        buff.append("1");
        // raw
        buff.append("Z");
        // examples
        buff.append("E");
        // Unicode
        buff.append("U");

        try {
            buff.append(URLEncoder.encode(criteria.getQueryString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // up to 100 sentences starting at 0
        buff.append("=0=");
        // use =1= to get random examples
        //        buff.append("=1=");

        System.out.println("searchString: " + buff.toString());
        return buff.toString();
    }
}
