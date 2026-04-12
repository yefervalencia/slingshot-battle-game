package com.slingshot.entities;

class DoubleScoreCrate extends CrateType {
    public DoubleScoreCrate() { super("DOUBLE_SCORE", 1); }
    @Override
    public void applyEffect(Player player) {
        // player.setScore(player.getScore() + 100);
    }
}
