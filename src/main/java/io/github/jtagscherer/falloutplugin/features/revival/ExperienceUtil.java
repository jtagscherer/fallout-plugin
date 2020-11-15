package io.github.jtagscherer.falloutplugin.features.revival;

import org.bukkit.entity.Player;

class ExperienceUtil {

    static void simulatePlayerDeath(Player player) {
        int droppedExperience = Math.max(7 * player.getLevel(), 100);
        int remainingExperience = Math.min(droppedExperience, ExperienceUtil.getExperience(player));

        ExperienceUtil.setExperience(player, remainingExperience);
    }

    private static void setExperience(Player player, int experience) {
        experience = Math.max(0, experience);
        double levelAndExperience = ExperienceUtil.getLevelFromExperience(experience);

        int level = (int) levelAndExperience;
        player.setLevel(level);
        player.setExp((float) (levelAndExperience - level));
    }

    private static int getExperience(Player player) {
        return ExperienceUtil.getExperienceFromLevel(player.getLevel())
                + Math.round(ExperienceUtil.getExperienceToNextLevel(player.getLevel()) * player.getExp());
    }

    private static double getLevelFromExperience(long experience) {
        if (experience > 1395) {
            return (Math.sqrt(72 * experience - 54215) + 325) / 18;
        } else if (experience > 315) {
            return Math.sqrt(40 * experience - 7839) / 10 + 8.1;
        } else if (experience > 0) {
            return Math.sqrt(experience + 9) - 3;
        }

        return 0;
    }

    private static int getExperienceFromLevel(int level) {
        if (level > 30) {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level > 15) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }

        return level * level + 6 * level;
    }

    private static int getExperienceToNextLevel(int level) {
        if (level > 30) {
            return 9 * level - 158;
        } else if (level > 15) {
            return 5 * level - 38;
        }

        return 2 * level + 7;
    }

}
