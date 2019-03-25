import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.constants.Banks;
import org.osbot.rs07.api.model.Character;
import org.osbot.rs07.api.model.GroundItem;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import org.osbot.rs07.utility.ConditionalSleep;

@ScriptManifest(author = "Sibbernski", info = "", logo = "", version = 1.0, name = "Sibbernski Ranged trainer")

public class Main extends Script {


    private static Area CHICKEN_FIELD = new Area(3171, 3300, 3184, 3291);
    private static Area COW_FIELD = new Area(3194, 3299, 3209, 3286);
    private static Area MONK_AREA = new Area(3044, 3498, 3059, 3483);

    private static Area currentArea;
    private static String currentNPCName;


    private String chicken = "Chicken";
    private String cow = "Cow";
    private String monk = "Monk";

    @Override
    public int onLoop() throws InterruptedException {

        if (!getInventory().contains("Trout")) {
            bankForFood();
        } else if (getInventory().contains("Iron knife")) {
            eguipAmmo();
        }
        if (getGroundItems().closest("Iron knife") != null && getGroundItems().closest("Iron knife").getAmount() >= 3) {
            pickUpAmmo();
        }
        if (getInventory().contains("Iron knife")) {
            eguipAmmo();
        }
        if (currentRangeLevel() < 10) {
            currentArea = CHICKEN_FIELD;
            currentNPCName = chicken;
        } else if (currentRangeLevel() >= 10 && currentRangeLevel() < 20) {
            currentArea = COW_FIELD;
            currentNPCName = cow;
        } else if (currentRangeLevel() >= 20 && currentRangeLevel() <= 60 && getInventory().contains("Trout")) {
            currentArea = MONK_AREA;
            currentNPCName = monk;
        }
        if (currentArea != null && currentNPCName != null) {
            attackNpc();
        }

        return 500;
    }

    private void attackNpc() throws InterruptedException {


        if (currentArea.contains(myPosition())) {
            if (getNpcs().closest(currentNPCName) != null && getNpcs().closest(currentNPCName).getHealthPercent() != 0 && !myPlayer().isUnderAttack()) {
                getNpcs().closest(currentNPCName).interact("Attack");
                new ConditionalSleep(2000) {
                    @Override
                    public boolean condition() {
                        return myPlayer().isInteracting(getNpcs().closest(currentNPCName)) || myPlayer().getHealthPercent() <= 40;
                    }
                }.sleep();
                while (myPlayer().isInteracting(getNpcs().closest(currentNPCName))) {
                    if (myPlayer().getHealthPercent() <= 40) {
                        eatFoodLowHealth();
                    }
                    sleep(1000);
                }
            }
        } else {
            getWalking().webWalk(currentArea);
        }
    }

    private void eatFoodLowHealth() {
        if (myPlayer().getHealthPercent() <= 40) {
            log("Health low eating food");
            getInventory().getItem("Trout").interact("Eat");
        }
    }

    private void pickUpAmmo() {

        if (currentArea.contains(myPosition())) {
            groundItems.closest("Iron knife").interact("Take");
            new ConditionalSleep(2000) {
                public boolean condition() {
                    return getInventory().contains("Iron knife");
                }
            }.sleep();
        }
    }


    private void eguipAmmo() {
        getInventory().getItem("Iron knife").interact("Wield");
    }

    private void bankForFood() throws InterruptedException {
        if (Banks.EDGEVILLE.contains(myPosition())) {
            if (getBank().isOpen()) {
                getBank().withdraw("Trout", 27);
            } else {
                getBank().open();
            }
        } else {
            getWalking().webWalk(Banks.EDGEVILLE);
        }
    }

    enum Monster {
        CHICKEN("Chicken", 1, new Area(3171, 3300, 3184, 3291)),
        COW("Cow", 10, new Area(3194, 3299, 3209, 3286)),
        MONK("Monk", 20, new Area(3044, 3498, 3059, 3483));

        String name;
        int levelRequired;
        Area area;

        Monster(String name, int levelRequired, Area area) {
            this.name = name;
            this.levelRequired = levelRequired;
            this.area = area;
        }
    }


    private int currentRangeLevel() {
        return getSkills().getDynamic(Skill.RANGED);
    }


}