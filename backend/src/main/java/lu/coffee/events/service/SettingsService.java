package lu.coffee.events.service;

import lu.coffee.events.entity.Setting;
import lu.coffee.events.repository.SettingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Réglages modifiables depuis l'espace admin (clé/valeur). Les valeurs par défaut sont ici. */
@Service
public class SettingsService {
    public static final Map<String, String> DEFAULTS = new LinkedHashMap<>();

    static {
        // Identité du café
        DEFAULTS.put("cafe.name", "Coffee Events");
        DEFAULTS.put("cafe.tagline", "Café de quartier, salle des fêtes, cuisine maison");
        DEFAULTS.put("cafe.address", "12 rue du Marché, Luxembourg");
        DEFAULTS.put("cafe.phone", "+352 20 00 00 00");
        DEFAULTS.put("cafe.email", "contact@coffee.lu");
        DEFAULTS.put("cafe.hours", "Lun-Ven 7h30-18h · Sam-Dim 9h-19h");
        DEFAULTS.put("cafe.about", "Un café chaleureux où l'on déjeune, où l'on travaille et où l'on fête. Notre salle privée accueille anniversaires, baptêmes, repas d'équipe et soirées.");
        DEFAULTS.put("brand.primary", "#b4531f");
        DEFAULTS.put("brand.dark", "#2a1810");
        DEFAULTS.put("currency", "EUR");
        // Commandes
        DEFAULTS.put("orders.enabled", "true");
        DEFAULTS.put("orders.dineIn.enabled", "true");
        DEFAULTS.put("orders.takeaway.enabled", "true");
        DEFAULTS.put("orders.takeaway.minMinutes", "15");
        DEFAULTS.put("orders.message", "");
        // Salle des fêtes
        DEFAULTS.put("room.enabled", "true");
        DEFAULTS.put("room.name", "Salle des fêtes");
        DEFAULTS.put("room.description", "Une salle privative de caractère, avec bar, sono et accès direct à la cuisine.");
        DEFAULTS.put("room.hourlyRate", "80");
        DEFAULTS.put("room.minHours", "2");
        DEFAULTS.put("room.maxHours", "10");
        DEFAULTS.put("room.maxGuests", "60");
        DEFAULTS.put("room.depositPercent", "30");
        DEFAULTS.put("room.openFrom", "10:00");
        DEFAULTS.put("room.openTo", "23:30");
        DEFAULTS.put("room.minNoticeDays", "2");
        DEFAULTS.put("room.bufferMinutes", "60");
        DEFAULTS.put("room.autoConfirm", "false");
        DEFAULTS.put("room.meals.enabled", "true");
        DEFAULTS.put("room.closedWeekdays", "");
        DEFAULTS.put("room.eventTypes", "Anniversaire,Baptême,Repas d'équipe,Mariage / fiançailles,Soirée privée,Autre");
        DEFAULTS.put("room.conditions", "Acompte réglé sur place à la confirmation. Annulation gratuite jusqu'à 7 jours avant l'événement.");
    }

    /** Textes affichés aux clients, traduisibles : clé de base + ".pt", ".lb", ".de" (vide = français). */
    public static final String[] TRANSLATABLE = {"cafe.tagline", "cafe.about", "cafe.hours", "room.name", "room.description", "room.conditions", "orders.message"};
    public static final String[] LANGS = {"pt", "lb", "de"};

    static {
        for (String k : TRANSLATABLE) for (String l : LANGS) DEFAULTS.put(k + "." + l, "");
    }

    private final SettingRepository repo;

    public SettingsService(SettingRepository repo) {
        this.repo = repo;
    }

    public Map<String, String> all() {
        Map<String, String> map = new LinkedHashMap<>(DEFAULTS);
        for (Setting s : repo.findAll()) {
            if (DEFAULTS.containsKey(s.key) && s.value != null) map.put(s.key, s.value);
        }
        return map;
    }

    public Map<String, String> update(Map<String, String> in) {
        for (Map.Entry<String, String> e : in.entrySet()) {
            if (!DEFAULTS.containsKey(e.getKey())) continue;
            Setting s = new Setting();
            s.key = e.getKey();
            s.value = e.getValue() == null ? "" : e.getValue();
            repo.save(s);
        }
        return all();
    }

    public String str(String key) {
        return all().getOrDefault(key, "");
    }

    public boolean bool(String key) {
        return "true".equalsIgnoreCase(str(key).trim());
    }

    public int integer(String key) {
        try {
            return Integer.parseInt(str(key).trim());
        } catch (NumberFormatException e) {
            return Integer.parseInt(DEFAULTS.getOrDefault(key, "0"));
        }
    }

    public BigDecimal dec(String key) {
        try {
            return new BigDecimal(str(key).trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
