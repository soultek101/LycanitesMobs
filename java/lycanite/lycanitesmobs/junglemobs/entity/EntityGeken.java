package lycanite.lycanitesmobs.junglemobs.entity;

import java.util.HashMap;

import lycanite.lycanitesmobs.DropRate;
import lycanite.lycanitesmobs.entity.EntityCreatureAgeable;
import lycanite.lycanitesmobs.entity.ai.EntityAIAttackMelee;
import lycanite.lycanitesmobs.entity.ai.EntityAILookIdle;
import lycanite.lycanitesmobs.entity.ai.EntityAISwimming;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetAttack;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetRevenge;
import lycanite.lycanitesmobs.entity.ai.EntityAIWander;
import lycanite.lycanitesmobs.entity.ai.EntityAIWatchClosest;
import lycanite.lycanitesmobs.junglemobs.JungleMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityGeken extends EntityCreatureAgeable implements IMob {
	
	private EntityAIAttackMelee meleeAttackAI;
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityGeken(World par1World) {
        super(par1World);
        
        // Setup:
        this.entityName = "Geken";
        this.mod = JungleMobs.instance;
        this.attribute = EnumCreatureAttribute.UNDEFINED;
        this.experience = 5;
        this.spawnsInDarkness = true;
        this.hasAttackSound = true;
        
        this.eggName = "JungleEgg";
        this.canGrow = false;
        this.babySpawnChance = 0.1D;
        
        this.setWidth = 0.6F;
        this.setHeight = 1.9F;
        this.setupMob();
        
        // Stats:
        this.attackPhaseMax = 3;
        this.justAttackedTime = 10;
    	
        // AI Tasks:
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        meleeAttackAI = new EntityAIAttackMelee(this).setRate(10);
        this.tasks.addTask(3, meleeAttackAI);
        this.tasks.addTask(6, new EntityAIWander(this).setPauseRate(30));
        this.tasks.addTask(10, new EntityAIWatchClosest(this).setTargetClass(EntityPlayer.class));
        this.tasks.addTask(11, new EntityAILookIdle(this));
        this.targetTasks.addTask(0, new EntityAITargetRevenge(this).setHelpCall(true));
        this.targetTasks.addTask(1, new EntityAITargetAttack(this).setTargetClass(EntityPlayer.class));
        this.targetTasks.addTask(2, new EntityAITargetAttack(this).setTargetClass(EntityVillager.class));
        
        // Drops:
        this.drops.add(new DropRate(Item.bone.itemID, 1).setMinAmount(1).setMaxAmount(2));
    }
    
    // ========== Stats ==========
	@Override
	protected void applyEntityAttributes() {
		HashMap<String, Double> baseAttributes = new HashMap<String, Double>();
		baseAttributes.put("maxHealth", 20D);
		baseAttributes.put("movementSpeed", 0.24D);
		baseAttributes.put("knockbackResistance", 0.0D);
		baseAttributes.put("followRange", 16D);
		baseAttributes.put("attackDamage", 2D);
        super.applyEntityAttributes(baseAttributes);
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        
        // Random Leaping:
        if(this.onGround && !this.worldObj.isRemote) {
        	if(this.hasAttackTarget()) {
        		if(this.rand.nextInt(10) == 0)
        			this.leap(6.0F, 0.6D, this.getAttackTarget());
        	}
        	else {
        		if(this.rand.nextInt(50) == 0 && this.isMoving())
        			this.leap(1.0D, 1.0D);
        	}
        }
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
	// ========== Melee Attack ==========
    @Override
    public boolean meleeAttack(Entity target, double damageScale) {
    	if(!super.meleeAttack(target, damageScale))
    		return false;
    	
    	// Poison:
        if(target instanceof EntityLivingBase) {
            byte effectSeconds = 5;
            if(this.worldObj.difficultySetting > 1)
                if (this.worldObj.difficultySetting == 2)
                	effectSeconds = 7;
                else if (this.worldObj.difficultySetting == 3)
                	effectSeconds = 10;
            if(effectSeconds > 0)
                ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.poison.id, effectSeconds * 20, 0));
        }
        
        // Update Phase:
        if(this.getAttackPhase() == 2)
        	meleeAttackAI.setRate(40);
        else
        	meleeAttackAI.setRate(10);
        this.nextAttackPhase();
        return true;
    }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect) {
        if(par1PotionEffect.getPotionID() == Potion.poison.id) return false;
        if(par1PotionEffect.getPotionID() == Potion.moveSlowdown.id) return false;
        super.isPotionApplicable(par1PotionEffect);
        return true;
    }
    
    @Override
    public float getFallResistance() {
    	return 100;
    }
	
	
	// ==================================================
  	//                      Breeding
  	// ==================================================
    // ========== Create Child ==========
    @Override
	public EntityCreatureAgeable createChild(EntityCreatureAgeable baby) {
		return new EntityGeken(this.worldObj);
	}
}
