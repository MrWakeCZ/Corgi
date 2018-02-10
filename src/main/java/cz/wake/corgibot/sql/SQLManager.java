package cz.wake.corgibot.sql;

import com.zaxxer.hikari.HikariDataSource;
import cz.wake.corgibot.CorgiBot;
import cz.wake.corgibot.objects.ChangeLog;
import cz.wake.corgibot.objects.TemporaryReminder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SQLManager {

    private final CorgiBot plugin;
    private final ConnectionPoolManager pool;
    private HikariDataSource dataSource;

    public SQLManager(CorgiBot plugin) {
        this.plugin = plugin;
        pool = new ConnectionPoolManager(plugin, "MySQL-Pool");
    }

    public void onDisable() {
        pool.closePool();
    }

    public ConnectionPoolManager getPool() {
        return pool;
    }

    public final void deletePrefix(final String guildId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("DELETE FROM corgibot.prefixes WHERE guild_id = ?");
            ps.setString(1, guildId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void updatePrefix(final String guildId, final String prefix) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("UPDATE corgibot.guild_data SET prefix = ? WHERE guild_id = ?;");
            ps.setString(1, prefix);
            ps.setString(2, guildId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void deleteIgnoredChannel(final String channelId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("DELETE FROM corgibot.ignored_channels WHERE channel_id = ?");
            ps.setString(1, channelId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void addReminder(final String userId, final long remindTime, final String message) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SET NAMES utf8mb4;INSERT INTO corgibot.reminders (user_id, remind_time, reminder) VALUES (?, ?, ?);");
            ps.setString(1, userId);
            ps.setLong(2, remindTime);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void deleteReminder(final String userId, final long remindTime) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("DELETE FROM corgibot.reminders WHERE user_id = ? AND remind_time = ?");
            ps.setString(1, userId);
            ps.setLong(2, remindTime);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void deleteReminderById(final String userId, final int reminderId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("DELETE FROM corgibot.reminders WHERE user_id = ? AND id = ?");
            ps.setString(1, userId);
            ps.setInt(2, reminderId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void addIgnoredChannel(final String guildId, final String channelId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("INSERT INTO corgibot.ignored_channels (guild_id, channel_id) VALUES (?, ?);");
            ps.setString(1, guildId);
            ps.setString(2, channelId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void registerUser(final User user) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("INSERT INTO corgibot.user_data (discord_id) VALUES (?);");
            ps.setString(1, user.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final boolean hasData(final User u) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT * FROM corgibot.user_data WHERE discord_id = '" + u.getId() + "';");
            ps.executeQuery();
            return ps.getResultSet().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final boolean existsGuildData(final String guildId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT COUNT(1) FROM corgibot.guild_data WHERE guild_id = '" + guildId + "';");
            ps.executeQuery();
            return ps.getResultSet().next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final String getPrefixOverSQL(final String guildId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT prefix FROM corgibot.prefixes WHERE guild_id = " + guildId + ";");
            ps.executeQuery();
            if (ps.getResultSet().next()) {
                return ps.getResultSet().getString("prefix");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
        return null;
    }

    public final Set<TextChannel> getIgnoredChannels(final String guildId) {
        Set<TextChannel> list = new HashSet<>();
        try {
            ResultSet set = CorgiBot.getInstance().getSql().getPool().getConnection().createStatement().executeQuery("SELECT channel_id FROM corgibot.ignored_channels WHERE guild_id = '" + guildId + "';");
            while (set.next()) {
                try {
                    list.add(CorgiBot.getJda().getGuildById(guildId).getTextChannelById(set.getString("channel_id")));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            set.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public final void insertDefaultServerData(final String guildId, final String prefix) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("INSERT INTO corgibot.guild_data (guild_id, prefix) VALUES (?,?);");
            ps.setString(1, guildId);
            ps.setString(2, prefix);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public final void deleteOldData(final String guildId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("DELETE FROM corgibot.prefixes WHERE guild_id = ?");
            ps.setString(1, guildId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
    }

    public HashSet<TemporaryReminder> getRemindersByUser(final String userId){
        HashSet<TemporaryReminder> list = new HashSet<>();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT * FROM corgibot.reminders WHERE user_id = ?;");
            ps.setString(1, userId);
            ps.executeQuery();
            while (ps.getResultSet().next()) {
                list.add(new TemporaryReminder(ps.getResultSet().getInt("id"), ps.getResultSet().getString("user_id"), ps.getResultSet().getLong("remind_time"), ps.getResultSet().getString("reminder")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
        return list;
    }

    public HashSet<TemporaryReminder> getAllReminders(){
        HashSet<TemporaryReminder> list = new HashSet<>();
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT * FROM corgibot.reminders;");
            ps.executeQuery();
            while (ps.getResultSet().next()) {
                list.add(new TemporaryReminder(ps.getResultSet().getInt("id"), ps.getResultSet().getString("user_id"), ps.getResultSet().getLong("remind_time"), ps.getResultSet().getString("reminder")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
        return list;
    }

    public final ChangeLog getLastChanges() {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = pool.getConnection();
            ps = conn.prepareStatement("SELECT * FROM corgibot.changelog;");
            ps.executeQuery();
            if (ps.getResultSet().next()) {
                return new ChangeLog(ps.getResultSet().getLong("date"),
                        ps.getResultSet().getString("news"),
                        ps.getResultSet().getString("fixes"),
                        ps.getResultSet().getString("warning"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, ps, null);
        }
        return null;
    }




}
