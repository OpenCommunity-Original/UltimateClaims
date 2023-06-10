package com.songoda.core.locale;

import com.songoda.core.chat.ChatMessage;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.utils.TextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;

/**
 * The Message object. This holds the message to be sent
 * as well as the plugins prefix so that they can both be
 * easily manipulated then deployed
 */
public class Message {
    private static boolean canActionBar = false;

    static {
        try {
            Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Player.Spigot.class.getDeclaredMethod("sendMessage", net.md_5.bungee.api.ChatMessageType.class, net.md_5.bungee.api.chat.TextComponent.class);

            canActionBar = true;
        } catch (Exception ignore) {
        }
    }

    private ChatMessage prefix = null;
    private ChatMessage message;

    /**
     * create a new message
     *
     * @param message the message text
     */
    public Message(String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.fromText(message);

        this.message = chatMessage;
    }

    /**
     * Format and send the held message to a player as a title messagexc
     *
     * @param sender command sender to send the message to
     */
    public void sendTitle(CommandSender sender) {
        if (sender instanceof Player) {
            if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11)) {
                ((Player) sender).sendTitle("", getMessage(), 10, 30, 10);
                return;
            }

            ((Player) sender).sendTitle("", getMessage());

            return;
        }

        sender.sendMessage(this.getMessage());
    }

    /**
     * Get and format the held message
     *
     * @return the message
     */
    public String getMessage() {
        return TextUtils.formatText(this.message.toText());
    }

    /**
     * Replace the provided placeholder with the provided object. <br />
     * Interchangeably Supports {@code %value%} and {@code {value}}
     *
     * @param placeholder the placeholder to replace
     * @param replacement the replacement object
     * @return the modified Message
     */
    public Message processPlaceholder(String placeholder, Object replacement) {
        final String place = Matcher.quoteReplacement(placeholder);
        this.message = message.replaceAll("%" + place + "%|\\{" + place + "\\}", replacement == null ? "" : Matcher.quoteReplacement(replacement.toString()));

        return this;
    }

    Message setPrefix(String prefix) {
        this.prefix = new ChatMessage();
        this.prefix.fromText(prefix + " ");

        return this;
    }

    @Override
    public String toString() {
        return this.message.toString();
    }

}
