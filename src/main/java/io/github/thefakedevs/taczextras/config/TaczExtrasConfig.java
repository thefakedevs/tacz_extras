package io.github.thefakedevs.taczextras.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class TaczExtrasConfig {
    public static final Common COMMON;
    public static final Client CLIENT;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    private TaczExtrasConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, "tacz_extras-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "tacz_extras-client.toml");
    }

    public static final class Common {
        public final ForgeConfigSpec.BooleanValue enableBulletImpactPatch;
        public final ForgeConfigSpec.BooleanValue enableBulletHitSounds;
        public final ForgeConfigSpec.BooleanValue enableBulletHitParticles;
        public final ForgeConfigSpec.BooleanValue enableWaterImpactEffects;
        public final ForgeConfigSpec.DoubleValue bulletHitSoundVolume;
        public final ForgeConfigSpec.IntValue bulletHitParticleCount;

        public final ForgeConfigSpec.BooleanValue enableGunSoundPatch;
        public final ForgeConfigSpec.BooleanValue useBundledGunSoundProfiles;
        public final ForgeConfigSpec.IntValue fireSoundDistanceOverride;

        public final ForgeConfigSpec.BooleanValue enableTargetSoundPatch;
        public final ForgeConfigSpec.DoubleValue targetSoundDistanceDivisor;

        public final ForgeConfigSpec.BooleanValue enableCrawlPatch;
        public final ForgeConfigSpec.BooleanValue crawlWithoutGun;
        public final ForgeConfigSpec.IntValue crawlCooldownTicks;
        public final ForgeConfigSpec.DoubleValue crawlFallDistance;

        public final ForgeConfigSpec.BooleanValue enableEnhancedBolting;
        public final ForgeConfigSpec.BooleanValue preserveBoltOnGunSwap;

        private Common(ForgeConfigSpec.Builder builder) {
            builder.push("bullet_impacts");
            enableBulletImpactPatch = builder
                    .comment("Apply the configurable bullet impact sound and particle patch.")
                    .define("enabled", true);
            enableBulletHitSounds = builder
                    .comment("Play material-aware sounds when TACZ bullets hit blocks.")
                    .define("sounds", true);
            enableBulletHitParticles = builder
                    .comment("Spawn block fragments when TACZ bullets hit blocks.")
                    .define("particles", true);
            enableWaterImpactEffects = builder
                    .comment("Use splash particles and water impact sounds for water hits.")
                    .define("water_effects", true);
            bulletHitSoundVolume = builder
                    .comment("Volume multiplier for the addon impact sounds.")
                    .defineInRange("sound_volume", 1.0, 0.0, 4.0);
            bulletHitParticleCount = builder
                    .comment("Base impact particle count. Metal hits spawn twice this many sparks.")
                    .defineInRange("particle_count", 4, 0, 64);
            builder.pop();

            builder.push("gun_sounds");
            enableGunSoundPatch = builder
                    .comment("Apply fire/silencer multipliers to volume and use the optional distance override.")
                    .define("enabled", true);
            useBundledGunSoundProfiles = builder
                    .comment("Apply the historical AWP, Glock 17, M1911, SKS Tactical and UMP45 profiles without replacing TACZ gun JSON files.")
                    .define("bundled_profiles", true);
            fireSoundDistanceOverride = builder
                    .comment("Base unsuppressed gunshot distance. Set to 0 to use TACZ's own common config value.")
                    .defineInRange("fire_distance_override", 512, 0, 8192);
            builder.pop();

            builder.push("target_block");
            enableTargetSoundPatch = builder
                    .comment("Reduce the TACZ target block hit sound range.")
                    .define("quiet_hit_sound", true);
            targetSoundDistanceDivisor = builder
                    .comment("TACZ target sound distance is divided by this value before being used as volume.")
                    .defineInRange("sound_distance_divisor", 64.0, 1.0, 1024.0);
            builder.pop();

            builder.push("crawl");
            enableCrawlPatch = builder
                    .comment("Enable configurable crawl behavior from the historical patches.")
                    .define("enabled", true);
            crawlWithoutGun = builder
                    .comment("Allow crawling while the main hand does not contain a TACZ gun.")
                    .define("without_gun", true);
            crawlCooldownTicks = builder
                    .comment("Cooldown between crawl state changes.")
                    .defineInRange("cooldown_ticks", 4, 0, 40);
            crawlFallDistance = builder
                    .comment("Cancel crawling after falling farther than this many blocks.")
                    .defineInRange("fall_distance", 0.75, 0.0, 16.0);
            builder.pop();

            builder.push("bolting");
            enableEnhancedBolting = builder
                    .comment("Retry failed client bolt synchronization and keep manual-action state stable.")
                    .define("enabled", true);
            preserveBoltOnGunSwap = builder
                    .comment("Continue the active server-side bolt operation using the original ItemStack after a weapon swap.")
                    .define("preserve_on_gun_swap", true);
            builder.pop();
        }
    }

    public static final class Client {
        public final ForgeConfigSpec.BooleanValue reloadCancelsAim;
        public final ForgeConfigSpec.BooleanValue hideHeadHitboxWithReducedDebugInfo;

        private Client(ForgeConfigSpec.Builder builder) {
            builder.push("controls");
            reloadCancelsAim = builder
                    .comment("Automatically leave ADS when a reload begins.")
                    .define("reload_cancels_aim", false);
            builder.pop();

            builder.push("debug");
            hideHeadHitboxWithReducedDebugInfo = builder
                    .comment("Do not render TACZ's headshot debug box when reduced debug information is enabled.")
                    .define("hide_head_hitbox_with_reduced_debug_info", true);
            builder.pop();
        }
    }
}
