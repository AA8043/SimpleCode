package org.a8043.simpleCode.frontend;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.a8043.simpleCode.api.ApiException;
import org.a8043.simpleCode.session.Session;
import org.a8043.simpleCode.session.content.AssistantContent;

import java.util.Base64;
import java.util.Date;
import java.util.List;

public class Mail {
    private static final String template = ResourceUtil.readUtf8Str("mail_template.html");
    private static final String iconBase64 = Base64.getEncoder().encodeToString(
        ResourceUtil.readBytes("icons/icon.png"));

    public static void sendStopWorking(long time, Session session, Session.Finish finish) {
        String timeString = DateUtil.format(new Date(time), "yyyy-MM-dd HH:mm:ss");
        String workedTimeString = FrontendUtil.formatDuration(finish.getWorkedTime());

        String summary = "";
        String contentTitle = "";
        String content = "";

        Object last = session.getAllContentList().getLast();
        if (last instanceof AssistantContent ac) {
            summary = I18n.get("mail.normal.summary", session.getName(), timeString, workedTimeString);
            contentTitle = I18n.get("mail.normal.title");
            List<Parser.ParserExtension> extensionList = List.of(
                TablesExtension.create(),
                FootnoteExtension.create(),
                StrikethroughSubscriptExtension.create(),
                AutolinkExtension.create(),
                TaskListExtension.create(),
                EmojiExtension.create()
            );
            content = HtmlRenderer.builder().extensions(extensionList).build().render(Parser.builder()
                .extensions(extensionList).build().parse(ac.getText()));
        } else if (last instanceof ApiException e) {
            summary = I18n.get("mail.apiErrorSummary", session.getName(),
                timeString, String.valueOf(e.getStatus()), workedTimeString);
            contentTitle = I18n.get("mail.error.title");
            content = e.getContent();
        } else if (last instanceof Exception e) {
            summary = I18n.get("mail.error.summary", session.getName(), timeString, workedTimeString);
            contentTitle = I18n.get("mail.error.title");
            content = e.getMessage();
        }

        String html = template
            .replace("{icon_base64}", iconBase64)
            .replace("{summary}", summary)
            .replace("{content_title}", contentTitle)
            .replace("{content}", content);

        MailAccount account = new MailAccount();
        FrontendSettings.MailSettings mail = FrontendSettings.INSTANCE.getMail();
        account.setHost(mail.getSmtpHost());
        account.setPort(mail.getSmtpPort());
        account.setFrom(mail.getFromAddress());
        account.setUser(mail.getSmtpUsername());
        account.setPass(mail.getSmtpPassword());
        account.setSslEnable(true);
        account.setCustomProperty("mail.smtp.ssl.protocols", "TLSv1.2");
        MailUtil.send(account, mail.getTo(),
            I18n.get("mail.stopWorkingTitle", session.getName()), html, true);
    }
}
