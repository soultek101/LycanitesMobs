package lycanite.lycanitesmobs.demonmobs.entity;

import java.util.HashMap;

import lycanite.lycanitesmobs.DropRate;
import lycanite.lycanitesmobs.ObjectLists;
import lycanite.lycanitesmobs.ObjectManager;
import lycanite.lycanitesmobs.demonmobs.DemonMobs;
import lycanite.lycanitesmobs.entity.EntityCreatureTameable;
import lycanite.lycanitesmobs.entity.ai.EntityAIAttackRanged;
import lycanite.lycanitesmobs.entity.ai.EntityAIBeg;
import lycanite.lycanitesmobs.entity.ai.EntityAIFollowOwner;
import lycanite.lycanitesmobs.entity.ai.EntityAIFollowParent;
import lycanite.lycanitesmobs.entity.ai.EntityAILookIdle;
import lycanite.lycanitesmobs.entity.ai.EntityAIMate;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetAttack;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetOwnerAttack;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetOwnerRevenge;
import lycanite.lycanitesmobs.entity.ai.EntityAITargetRevenge;
import lycanite.lycanitesmobs.entity.ai.EntityAITempt;
import lycanite.lycanitesmobs.entity.ai.EntityAIWander;
import lycanite.lycanitesmobs.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityCacodemon extends EntityCreatureTameable {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCacodemon(World par1World) {
        super(par1World);
        
        // Setup:
        this.entityName = "Cacodemon";
        this.mod = DemonMobs.instance;
        this.attribute = EnumCreatureAttribute.UNDEAD;
        this.experience = 5;
        this.hasAttackSound = false;
        
        this.eggName = "DemonEgg";
        
        this.setWidth = 1.9F;
        this.setHeight = 1.9F;
        
        this.justAttackedTime = 20;
        this.setupMob();
        
        // Stats:
        this.rangedDamage = new int[] {8, 10, 14};
        
        // AI Tasks:
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(1, new EntityAIMate(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAIFollowOwner(this).setStrayDistance(4).setLostDistance(32));
        this.tasks.addTask(4, new EntityAITempt(this).setItemID(ObjectManager.getItem("HellfireCharge").itemID).setTemptDistanceMin(4.0D));
        this.tasks.addTask(5, new EntityAIAttackRanged(this).setSpeed(0.25D).setRate(80).setRange(40.0F).setMinChaseDistance(10.0F).setLongMemory(false));
        this.tasks.addTask(6, new EntityAIFollowParent(this));
        this.tasks.addTask(6, new EntityAIWander(this).setPauseRate(30));
        this.tasks.addTask(9, new EntityAIBeg(this));
        this.tasks.addTask(10, new EntityAIWatchClosest(this).setTargetClass(EntityPlayer.class));
        this.tasks.addTask(11, new EntityAILookIdle(this));
        this.targetTasks.addTask(0, new EntityAITargetOwnerRevenge(this));
        this.targetTasks.addTask(1, new EntityAITargetOwnerAttack(this));
        this.targetTasks.addTask(2, new EntityAITargetRevenge(this));
        this.targetTasks.addTask(3, new EntityAITargetAttack(this).setTargetClass(EntityPlayer.class));
        this.targetTasks.addTask(3, new EntityAITargetAttack(this).setTargetClass(EntityVillager.class));
                
        // Drops:
        this.drops.add(new DropRate(Item.ghastTear.itemID, 0.25F).setMinAmount(1).setMaxAmount(3));
        this.drops.add(new DropRate(Item.gunpowder.itemID, 0.5F).setMinAmount(1).setMaxAmount(3));
        this.drops.add(new DropRate(Item.blazePowder.itemID, 0.5F).setMinAmount(1).setMaxAmount(3));
        this.drops.add(new DropRate(ObjectManager.getItem("DemonicLightningCharge").itemID, 0.25F));
    }
    
    // ========== Stats ==========
	@Override
	protected void applyEntityAttributes() {
		HashMap<String, Double> baseAttributes = new HashMap<String, Double>();
		if(this.isTamed())
			baseAttributes.put("maxHealth", 60D);
		else
			baseAttributes.put("maxHealth", 40D);
		baseAttributes.put("movementSpeed", 0.24D);
		baseAttributes.put("knockbackResistance", 0.0D);
		baseAttributes.put("followRange", 40D);
		baseAttributes.put("attackDamage", 0D);
        super.applyEntityAttributes(baseAttributes);
    }
	
	
	// ==================================================
  	//                     Abilities
  	// ==================================================
    // ========== Movement ==========
    public boolean canFly() { return true; }
    
    
    // ==================================================
    //                     Equipment
    // ==================================================
    public int getNoBagSize() { return 0; }
    public int getBagSize() { return 5; }
	
	
	// ==================================================
   	//                      Attacks
   	// ==================================================
	// ========== Ranged Attack ==========
    @Override
    public void rangedAttack(Entity target, float range) {
    	// Type:
    	EntityDemonicBlast projectile = new EntityDemonicBlast(this.worldObj, this);
        projectile.setProjectileScale(1f);
    	
    	// Y Offset:
    	projectile.posY -= this.height * 0.5D;
    	
    	// Set Velocities:
        double d0 = target.posX - this.posX;
        double d1 = target.posY - projectile.posY;
        double d2 = target.posZ - this.posZ;
        float f1 = MathHelper.sqrt_double(d0 * d0 + d2 * d2) * 0.1F;
        float velocity = 0.5F;
        projectile.setThrowableHeading(d0, d1 + (double)f1, d2, velocity, 0.0F);
        
        // Damage:
        projectile.setDamage(rangedDamage[0]);
        if(worldObj.difficultySetting == 2) projectile.setDamage(rangedDamage[1]);
        else if(worldObj.difficultySetting > 2) projectile.setDamage(rangedDamage[2]);
        
        // Launch:
        this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.worldObj.spawnEntityInWorld(projectile);

        super.rangedAttack(target, range);
    }
    
    
    // ==================================================
    //                     Immunities
    // ==================================================
    @Override
    public boolean isDamageEntityApplicable(Entity entity) {
    	if(entity instanceof EntityCacodemon)
    		return false;
    	return super.isDamageEntityApplicable(entity);
    }
    @Override
    public boolean isPotionApplicable(PotionEffect par1PotionEffect) {
        if(par1PotionEffect.getPotionID() == Potion.wither.id) return false;
        super.isPotionApplicable(par1PotionEffect);
        return true;
    }
    
    @Override
    public boolean canBurn() { return false; }
    
    
    // ==================================================
    //                       Taming
    // ==================================================
    @Override
    public boolean isTamingItem(ItemStack itemstack) {
        return itemstack.itemID == ObjectManager.getItem("HellfireCharge").itemID;
    }
    
    @Override
    public void setTamed(boolean setTamed) {
    	if(setTamed)
    		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(60.0D);
    	else
    		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(40.0D);
    	super.setTamed(setTamed);
    }
    
    
    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness(float par1) {
        if(justAttacked())
        	return 1.0F;
        else
        	return super.getBrightness(par1);
    }
    
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float par1) {
        if(justAttacked())
        	return 15728880;
        else
        	return super.getBrightnessForRender(par1);
    }
    
    
    // ==================================================
    //                       Healing
    // ==================================================
    // ========== Healing Item ==========
    @Override
    public boolean isHealingItem(ItemStack testStack) {
    	return ObjectLists.inItemList("CookedMeat", testStack);
    }
}
