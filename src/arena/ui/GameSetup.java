package arena.ui;

public class GameSetup {
    public String playerClass;
    public String itemSlot1;
    public String itemSlot2;
    public String difficulty;

    public GameSetup(String playerClass, String difficulty) {
        this.playerClass = playerClass;
        this.itemSlot1 = "None";
        this.itemSlot2 = "None";
        this.difficulty = difficulty;
    }

    public String getItemAtSlot(int slotIndex) {
        return slotIndex == 0 ? itemSlot1 : itemSlot2;
    }

    public void setItemAtSlot(int slotIndex, String itemName) {
        String normalized = (itemName == null || itemName.isBlank()) ? "None" : itemName;
        if (slotIndex == 0) {
            itemSlot1 = normalized;
        } else {
            itemSlot2 = normalized;
        }
    }
}
