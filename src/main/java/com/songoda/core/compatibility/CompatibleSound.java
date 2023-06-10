package com.songoda.core.compatibility;

import org.bukkit.Sound;

/**
 * TODO: Probably recode to be similar to CompatibleMaterial
 * <p>
 * Sounds that are compatible with server versions 1.7+ <br>
 * TODO: This needs work.
 * Finished 1.8, finished 1.9 blocks, resume with 1.9 entities<br>
 * Between 1.8 and 1.9, all sounds were renamed, and between 1.12 and 1.13, some
 * sounds were renamed. New sounds have been added by different versions, as
 * well. The intent of this class is to provide either the correct sound or a
 * near equivalent for the current server.
 */
public enum CompatibleSound {
    // some of these values are missing an API value...
    // would using the raw strings be better?
    // 1.8 list:
    // https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments


    // "ITEM_SWEET_BERRIES_PICK_FROM_BUSH"

    // I have no idea...
    // I have no idea...
    // This value disappeared from the API from 1.9-1.11 (returned in 12)
    // these may be reversed...
    ENTITY_BLAZE_DEATH("BLAZE_DEATH"),
    // ENTITY_MAGMACUBE_DEATH
    // ENTITY_MAGMACUBE_HURT
    ENTITY_PLAYER_LEVELUP("LEVEL_UP"),
    // Not sure which is 1 or 2
    // this is missing from 1.8 API for some reason
    // records are missing from 1.8 API
    UI_BUTTON_CLICK("CLICK");

    private static final boolean DEBUG = false;
    private /* final */ Sound sound;
    private /* final */ boolean compatibilityMode;

    CompatibleSound() {
        // This could get risky, since we haven't finished this
        //sound = Sound.valueOf(name());
        Sound find = null;
        for (Sound s : Sound.values()) {
            if (s.name().equals(name())) {
                find = s;
                break;
            }
        }
        if (DEBUG && find == null) {
            System.err.println("Sound for " + name() + " not found!");
        }
        sound = find;
        compatibilityMode = find == null;
    }

    // if the sound only ever changed from 1.8 -> 1.9
    CompatibleSound(String compatibility_18) {
        try {
            compatibilityMode = false;

            if (ServerVersion.isServerVersionBelow(ServerVersion.V1_9)) {
                sound = Sound.valueOf(compatibility_18);
            } else {
                sound = Sound.valueOf(name());
            }
        } catch (Exception ex) {
            System.err.println("ERROR loading " + name());
            ex.printStackTrace();
        }
    }

    CompatibleSound(Version... versions) {
        try {
            for (Version v : versions) {
                if (v.sound != null && ServerVersion.isServerVersionAtLeast(v.version)) {
                    sound = Sound.valueOf(v.sound);
                    compatibilityMode = v.compatibilityMode;
                    return;
                }
            }
        } catch (Exception ex) {
            System.err.println("ERROR loading " + name());
            for (Version v : versions) {
                System.err.println(v.version + " - " + v.sound);
            }

            ex.printStackTrace();
        }

        Sound find = null;
        for (Sound s : Sound.values()) {
            if (s.name().equals(name())) {
                find = s;
                break;
            }
        }

        sound = find;
        compatibilityMode = find == null;
    }

    CompatibleSound(ServerVersion minVersion, Version... versions) {
        try {
            if (ServerVersion.isServerVersionAtLeast(minVersion)) {
                // should be good to use this sound
                sound = Sound.valueOf(name());
            } else {
                for (Version v : versions) {
                    if (v.sound != null && ServerVersion.isServerVersionAtLeast(v.version)) {
                        sound = Sound.valueOf(v.sound);
                        compatibilityMode = v.compatibilityMode;
                        return;
                    }
                }

                sound = null;
            }

            compatibilityMode = false;
        } catch (Exception ex) {
            System.err.println("ERROR loading " + name() + " (" + minVersion + ")");
            for (Version v : versions) {
                System.err.println(v.version + " - " + v.sound);
            }

            ex.printStackTrace();
        }
    }

    private static Version v(String sound) {
        return new Version(ServerVersion.UNKNOWN, sound, false);
    }

    private static Version v(ServerVersion version, String sound) {
        return new Version(version, sound, false);
    }

    private static Version v(ServerVersion version, String sound, boolean compatibility) {
        return new Version(version, sound, compatibility);
    }

    private static Version v(String sound, boolean compatibility) {
        return new Version(ServerVersion.UNKNOWN, sound, compatibility);
    }

    /**
     * Returns the appropriate Bukkit API sound or tries to find a sane alternative,
     * if the server does not support that sound
     *
     * @return Either the matching sound or a similar sound
     */
    public Sound getSound() {
        return sound != null ? sound : UI_BUTTON_CLICK.sound;
    }

    private static class Version {
        final ServerVersion version;
        final String sound;
        final boolean compatibilityMode;

        public Version(ServerVersion version, String sound, boolean compatibility) {
            this.version = version;
            this.sound = sound;
            this.compatibilityMode = compatibility;
        }
    }
}
