package lycanite.lycanitesmobs.entity.ai;

import lycanite.lycanitesmobs.entity.EntityCreatureBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAISwimming extends EntityAIBase {
	// Targets:
    private EntityCreatureBase host;
    
    // Properties:
    private boolean sink = false;
    
    // ==================================================
   	//                    Constructor
   	// ==================================================
    public EntityAISwimming(EntityCreatureBase setEntity) {
        this.host = setEntity;
        this.setMutexBits(4);
        setEntity.getNavigator().setCanSwim(true);
    }
    
    
    // ==================================================
  	//                  Set Properties
  	// ==================================================
    public EntityAISwimming setSink(boolean setSink) {
    	this.sink = setSink;
    	return this;
    }
    
    
    // ==================================================
   	//                  Should Execute
   	// ==================================================
    public boolean shouldExecute() {
        return this.host.isInWater() || this.host.handleLavaMovement();
    }
    
    
    // ==================================================
   	//                      Update
   	// ==================================================
    public void updateTask() {
    	if(host.canSwim() && host.getAttackTarget() != null && host.posY < host.getAttackTarget().posY)
    		this.host.getJumpHelper().setJumping();
    	else if(!sink && this.host.getRNG().nextFloat() < 0.8F)
            this.host.getJumpHelper().setJumping();
    }
}
