package com.alaka_ala.florafilm.utils.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Keep;

import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter;
import com.alaka_ala.florafilm.fragments.filmDetails.SelectorVoiceAdapter.Folder;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**Класс предназначен для отслеживания изменений в структуре контента.
 * Отслеживает структуру AdapterData - {@link SelectorVoiceAdapter.AdapterData}
 * Работает для отслеживания изменений в фильмах которые пользователь отметил как "Уведомить о новых озвучках/Сериях/Сезонах"*/
@Keep
public class ContentStructureTracker {
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public ContentStructureTracker(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Главный метод проверки изменений
    public boolean hasStructureChanged(String kinopoiskId, boolean isSerial, List<Folder> rootFolders) {
        String oldSignature = prefs.getString(getSignatureKey(kinopoiskId), "");
        String newSignature = calculateSignature(isSerial, rootFolders);

        if (!newSignature.equals(oldSignature)) {
            prefs.edit().putString(getSignatureKey(kinopoiskId), newSignature).apply();
            return true;
        }
        return false;
    }

    private String getSignatureKey(String kinopoiskId) {
        return "content_structure_" + kinopoiskId;
    }

    private String calculateSignature(boolean isSerial, List<Folder> rootFolders) {
        if (isSerial) {
            return calculateSerialSignature(rootFolders);
        } else {
            return calculateMovieSignature(rootFolders);
        }
    }

    // Сигнатура для сериала: Балансер > Сезон > Серия > Озвучка > Качество
    private String calculateSerialSignature(List<Folder> rootFolders) {
        StringBuilder sig = new StringBuilder();

        for (Folder balancer : rootFolders) {
            sig.append("BALANCER:").append(balancer.name).append("\n");

            if (balancer.children != null) {
                for (Object seasonObj : balancer.children) {
                    if (seasonObj instanceof Folder) {
                        Folder season = (Folder) seasonObj;
                        sig.append("  SEASON:").append(season.name)
                                .append("|episodes:").append(countEpisodes(season))
                                .append("\n");

                        // Обрабатываем серии
                        if (season.children != null) {
                            for (Object episodeObj : season.children) {
                                if (episodeObj instanceof Folder) {
                                    Folder episode = (Folder) episodeObj;
                                    sig.append("    EPISODE:").append(episode.name)
                                            .append("|voices:").append(countVoices(episode))
                                            .append("\n");

                                    // Обрабатываем озвучки
                                    if (episode.children != null) {
                                        List<String> voiceSignatures = new ArrayList<>();
                                        for (Object voiceObj : episode.children) {
                                            if (voiceObj instanceof Folder) {
                                                Folder voice = (Folder) voiceObj;
                                                String voiceSig = processVoiceForSerial(voice);
                                                voiceSignatures.add(voiceSig);
                                            }
                                        }
                                        Collections.sort(voiceSignatures);
                                        for (String voiceSig : voiceSignatures) {
                                            sig.append("      ").append(voiceSig).append("\n");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return hashString(sig.toString());
    }

    // Сигнатура для фильма: Балансер > Озвучка > Качество
    private String calculateMovieSignature(List<Folder> rootFolders) {
        StringBuilder sig = new StringBuilder();

        for (Folder balancer : rootFolders) {
            sig.append("BALANCER:").append(balancer.name).append("\n");

            if (balancer.children != null) {
                List<String> voiceSignatures = new ArrayList<>();

                for (Object voiceObj : balancer.children) {
                    if (voiceObj instanceof Folder) {
                        Folder voice = (Folder) voiceObj;
                        String voiceSig = processVoiceForMovie(voice);
                        voiceSignatures.add(voiceSig);
                    }
                }

                Collections.sort(voiceSignatures);
                for (String voiceSig : voiceSignatures) {
                    sig.append("  ").append(voiceSig).append("\n");
                }
            }
        }

        return hashString(sig.toString());
    }

    private String processVoiceForSerial(Folder voice) {
        StringBuilder sig = new StringBuilder();
        sig.append("VOICE:").append(voice.name);

        if (voice.children != null) {
            List<String> qualitySignatures = new ArrayList<>();
            for (Object qualityObj : voice.children) {
                if (qualityObj instanceof SelectorVoiceAdapter.File) {
                    SelectorVoiceAdapter.File file = (SelectorVoiceAdapter.File) qualityObj;
                    // Используем только имя и разрешение, без URL
                    qualitySignatures.add("QUALITY:" + file.name + "|res " + file.indexPath + "unknown");
                }
            }
            Collections.sort(qualitySignatures);
            for (String qualitySig : qualitySignatures) {
                sig.append("[").append(qualitySig).append("]");
            }
        }

        return sig.toString();
    }

    private String processVoiceForMovie(Folder voice) {
        StringBuilder sig = new StringBuilder();
        sig.append("VOICE:").append(voice.name);

        if (voice.children != null) {
            List<String> qualitySignatures = new ArrayList<>();
            for (Object qualityObj : voice.children) {
                if (qualityObj instanceof SelectorVoiceAdapter.File) {
                    SelectorVoiceAdapter.File file = (SelectorVoiceAdapter.File) qualityObj;
                    qualitySignatures.add("QUALITY:" + file.name);
                }
            }
            Collections.sort(qualitySignatures);
            for (String qualitySig : qualitySignatures) {
                sig.append("[").append(qualitySig).append("]");
            }
        }

        return sig.toString();
    }

    private int countEpisodes(Folder season) {
        int count = 0;
        if (season.children != null) {
            for (Object child : season.children) {
                if (child instanceof Folder) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countVoices(Folder episode) {
        int count = 0;
        if (episode.children != null) {
            for (Object child : episode.children) {
                if (child instanceof Folder) {
                    count++;
                }
            }
        }
        return count;
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    /**Если первый метод будет ложно срабатывать, то можно попробовать данный метод.</br>
     * Он проще, считает только изменения в структуре.*/
    public String getQuickSignature(boolean isSerial, List<Folder> rootFolders) {
        StringBuilder sig = new StringBuilder();

        for (Folder balancer : rootFolders) {
            sig.append(balancer.name).append("|");

            if (isSerial) {
                // Для сериала: считаем количество сезонов, серий и озвучек
                int seasons = 0, episodes = 0, voices = 0;

                if (balancer.children != null) {
                    seasons = balancer.children.size();
                    for (Object season : balancer.children) {
                        if (season instanceof Folder) {
                            Folder seasonFolder = (Folder) season;
                            if (seasonFolder.children != null) {
                                episodes += seasonFolder.children.size();
                                for (Object episode : seasonFolder.children) {
                                    if (episode instanceof Folder) {
                                        Folder episodeFolder = (Folder) episode;
                                        if (episodeFolder.children != null) {
                                            voices += episodeFolder.children.size();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                sig.append("SEASONS:").append(seasons)
                        .append("|EPISODES:").append(episodes)
                        .append("|VOICES:").append(voices);
            } else {
                // Для фильма: считаем количество озвучек и качеств
                int voices = 0, qualities = 0;

                if (balancer.children != null) {
                    voices = balancer.children.size();
                    for (Object voice : balancer.children) {
                        if (voice instanceof Folder) {
                            Folder voiceFolder = (Folder) voice;
                            if (voiceFolder.children != null) {
                                qualities += voiceFolder.children.size();
                            }
                        }
                    }
                }
                sig.append("VOICES:").append(voices)
                        .append("|QUALITIES:").append(qualities);
            }
            sig.append(";");
        }

        return sig.toString();
    }

}
