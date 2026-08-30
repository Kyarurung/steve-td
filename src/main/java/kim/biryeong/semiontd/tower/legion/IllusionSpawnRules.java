package kim.biryeong.semiontd.tower.legion;

final class IllusionSpawnRules {
    private IllusionSpawnRules() {
    }

    static int dueTick(int currentTick, int index, int cloneCount, int spreadTicks) {
        int delayTicks = (int) Math.floor(index * (double) spreadTicks / cloneCount);
        return currentTick + delayTicks + (delayTicks > 0 ? 1 : 0);
    }
}
