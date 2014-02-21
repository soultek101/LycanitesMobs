package lycanite.lycanitesmobs.demonmobs.entity;

import lycanite.lycanitesmobs.AssetManager;
import lycanite.lycanitesmobs.api.ICustomProjectile;
import lycanite.lycanitesmobs.api.ILycaniteMod;
import lycanite.lycanitesmobs.demonmobs.DemonMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityDemonicBlast extends EntityThrowable implements ICustomProjectile {
	public String entityName = "DemonicBlast";
	public ILycaniteMod mod = DemonMobs.instance;
	
	// Properties:
	public Entity shootingEntity;
	byte damage = 8;
	private float projectileScale = 2.5f;
	private float projectileWidth = 1f;
	private float projectileHeight = 1f;
	public int expireTime = 15;
	
	// Rapid Fire:
	private int rapidTicks = 0;
	private int rapidDelay = 5;
	
    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityDemonicBlast(World par1World) {
        super(par1World);
        this.setSize(projectileWidth, projectileHeight);
    }

    public EntityDemonicBlast(World par1World, EntityLivingBase par2EntityLivingBase) {
        super(par1World, par2EntityLivingBase);
        this.setSize(projectileWidth, projectileHeight);
    }

    public EntityDemonicBlast(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6);
        this.setSize(projectileWidth, projectileHeight);
    }
	
    
    // ==================================================
 	//                   Update
 	// ==================================================
    @Override
    public void onUpdate() {
    	super.onUpdate();
    	if(!this.worldObj.isRemote) {
	    	if(rapidTicks % 5 == 0 && !isDead) {
	    		fireProjectile();
	    		fireProjectile();
	    	}
	    	if(rapidTicks == Integer.MAX_VALUE)
	    		rapidTicks = -1;
	    	rapidTicks++;
    	}
    	
    	if(this.posY > this.worldObj.getHeight() + 20)
    		this.setDead();
    	
    	if(this.ticksExisted >= this.expireTime * 20)
    		this.setDead();
    }
	
    
    // ==================================================
 	//                 Fire Projectile
 	// ==================================================
    public void fireProjectile() {
    	World world = this.worldObj;
    	
		IProjectile projectile;
		if(this.getThrower() != null) {
			projectile = (IProjectile) new EntityDemonicSpark(world, this.getThrower());
			if(projectile instanceof Entity) {
				((Entity)projectile).posX = this.posX;
				((Entity)projectile).posY = this.posY;
				((Entity)projectile).posZ = this.posZ;
			}
		}
		else
			projectile = (IProjectile) new EntityDemonicSpark(world, this.posX, this.posY, this.posZ);
		float velocity = 1.2F;
		double motionT = this.motionX + this.motionY + this.motionZ;
		if(this.motionX < 0) motionT -= this.motionX * 2;
		if(this.motionY < 0) motionT -= this.motionY * 2;
		if(this.motionZ < 0) motionT -= this.motionZ * 2;
        projectile.setThrowableHeading(this.motionX / motionT + (rand.nextGaussian() - 0.5D), this.motionY / motionT + (rand.nextGaussian() - 0.5D), this.motionZ / motionT + (rand.nextGaussian() - 0.5D), velocity, 0);
        
        if(projectile instanceof ICustomProjectile)
        	this.playSound(((ICustomProjectile) projectile).getLaunchSound(), 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
        
        world.spawnEntityInWorld((Entity)projectile);
    }
	
    
    // ==================================================
 	//                   Movement
 	// ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravityVelocity() {
        return 0.0001F;
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    @Override
    protected void onImpact(MovingObjectPosition movingObjectPos) {
    	// Entity Hit:
    	if(movingObjectPos.entityHit != null) {
    		boolean doDamage = true;
			if(movingObjectPos.entityHit instanceof EntityLivingBase) {
				EntityLivingBase owner = this.getThrower();
			    if(this.getThrower() != null && owner instanceof EntityPlayer) {
			    	if(MinecraftForge.EVENT_BUS.post(new AttackEntityEvent((EntityPlayer)owner, movingObjectPos.entityHit))) {
			    		doDamage = false;
			    	}
			    }
			}
			if(doDamage) {
				movingObjectPos.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)damage);
	    		if(movingObjectPos.entityHit instanceof EntityLivingBase)
	    			((EntityLivingBase)movingObjectPos.entityHit).addPotionEffect(new PotionEffect(Potion.wither.id, 10 * 20, 3));
			}
    	}
    	
    	// Impact Particles:
        for(int i = 0; i < 8; ++i) {
        	fireProjectile();
            this.worldObj.spawnParticle("reddust", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        }
        
        // Remove Projectile:
        if(!this.worldObj.isRemote) {
            this.setDead();
        }
    }
    
    
    // ==================================================
 	//                    Collision
 	// ==================================================
    public boolean canBeCollidedWith() {
        return false;
    }
    
    
    // ==================================================
 	//                     Attacked
 	// ==================================================
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        return false;
    }
    
    
    // ==================================================
 	//                      Scale
 	// ==================================================
    @Override
    public void setProjectileScale(float newScale) {
    	projectileScale = newScale;
    }
    
    @Override
    public float getProjectileScale() {
        return projectileScale;
    }
    
    
    // ==================================================
 	//                      Damage
 	// ==================================================
    @Override
    public void setDamage(int newDamage) {
    	damage = (byte)newDamage;
    }
    
    @Override
    public float getDamage() {
        return (float)damage;
    }
    
    
    // ==================================================
 	//                      Visuals
 	// ==================================================
    @Override
    public ResourceLocation getTexture() {
    	if(AssetManager.getTexture(this.entityName) == null)
    		AssetManager.addTexture(this.entityName, this.mod.getDomain(), "textures/items/" + this.entityName.toLowerCase() + ".png");
    	return AssetManager.getTexture(this.entityName);
    }
    
    
    // ==================================================
 	//                      Sounds
 	// ==================================================
    @Override
    public String getLaunchSound() {
    	return AssetManager.getSound("DemonicBlast");
    }
    
    
    // ==================================================
    //                   Brightness
    // ==================================================
    public float getBrightness(float par1) {
        return 1.0F;
    }
    
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float par1) {
        return 15728880;
    }
}
