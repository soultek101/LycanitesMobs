package lycanite.lycanitesmobs.entity;

import lycanite.lycanitesmobs.api.ICustomProjectile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityLaserEnd extends EntityThrowable implements ICustomProjectile {
	// Laser End:
	private double targetX;
	private double targetY;
	private double targetZ;
	private int posIDStart = 13;
	
	// Properties:
	public EntityLivingBase shootingEntity;
	public EntityLaser laserEntity;
	private float projectileScale = 1f;
	private float projectileWidth = 0.2f;
	private float projectileHeight = 0.2f;
	private double projectileSpeed;
	
    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityLaserEnd(World world) {
        super(world);
        this.setStats();
    }

    public EntityLaserEnd(World world, double par2, double par4, double par6, EntityLaser laser) {
        super(world, par2, par4, par6);
        this.laserEntity = laser;
        this.setStats();
    }
    
    public EntityLaserEnd(World world, EntityLivingBase shooter, EntityLaser laser) {
        super(world, shooter);
        this.shootingEntity = shooter;
        this.laserEntity = laser;
        this.setStats();
    }
    
    public void setStats() {
        this.setSpeed(1.0D);
        this.setSize(projectileWidth, projectileHeight);
        if(laserEntity != null) {
	        this.targetX = this.laserEntity.posX;
	        this.targetY = this.laserEntity.posY;
	        this.targetZ = this.laserEntity.posZ;
        }
        this.dataWatcher.addObject(this.posIDStart, (float)this.posX);
        this.dataWatcher.addObject(this.posIDStart + 1, (float)this.posY);
        this.dataWatcher.addObject(this.posIDStart + 2, (float)this.posZ);
    }
    
    
    // ==================================================
 	//                     Updates
 	// ==================================================
    // ========== Main Update ==========
    @Override
    public void onUpdate() {
    	if(this.worldObj.isRemote) {
    		this.posX = this.dataWatcher.getWatchableObjectFloat(this.posIDStart);
    		this.posY = this.dataWatcher.getWatchableObjectFloat(this.posIDStart + 1);
    		this.posZ = this.dataWatcher.getWatchableObjectFloat(this.posIDStart + 2);
    		return;
    	}
    	
    	if((this.laserEntity == null || !this.laserEntity.isEntityAlive()) && !this.isDead)
    		this.setDead();
    	
    	if(this.isEntityAlive())
    		this.moveToTarget();
    	
    	this.dataWatcher.updateObject(this.posIDStart, (float)this.posX);
    	this.dataWatcher.updateObject(this.posIDStart + 1, (float)this.posY);
    	this.dataWatcher.updateObject(this.posIDStart + 2, (float)this.posZ);
    }
    
    // ========== End Update ==========
	public void onUpdateEnd(double newTargetX, double newTargetY, double newTargetZ) {
		if(this.worldObj.isRemote)
			return;
		
		this.targetX = newTargetX;
		this.targetY = newTargetY;
		this.targetZ = newTargetZ;
		
		this.playSound(this.getLaunchSound(), 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
	}
	
    
    // ==================================================
 	//                   Movement
 	// ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravityVelocity() {
        return 0.0F;
    }
    
    // ========== Move to Target ==========
    public void moveToTarget() {
    	this.posX = moveCoordToTarget(this.posX, this.targetX, this.laserEntity.posX);
    	this.posY = moveCoordToTarget(this.posY, this.targetY, this.laserEntity.posY);
    	this.posZ = moveCoordToTarget(this.posZ, this.targetZ, this.laserEntity.posZ);
    	// TODO Fix speeds.
    	//this.posX = this.targetX;
    	//this.posY = this.targetY;
    	//this.posZ = this.targetZ;
    }
    
    // ========== Move Coord ==========
    public double moveCoordToTarget(double coord, double targetCoord, double originCoord) {
    	double distance = targetCoord - coord;
    	double moveSpeed = this.projectileSpeed;
    	if(distance > 0) {
    		if(distance < moveSpeed + 1)
    			moveSpeed = distance;
    		if((targetCoord - originCoord) > (coord - originCoord))
    			return coord + moveSpeed;
    		else
    			return targetCoord;
    	}
    	else if(distance < 0) {
    		if(distance > -moveSpeed - 1)
    			moveSpeed = -distance;
    		if((targetCoord - originCoord) < (coord - originCoord))
    			return coord - moveSpeed;
    		else
    			return targetCoord;
    	}
    	return targetCoord;
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    @Override
    protected void onImpact(MovingObjectPosition movingObjectPos) {}
    
    
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
 	//                      Speed
 	// ==================================================
    public void setSpeed(double speed) {
    	this.projectileSpeed = speed;
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
    public void setDamage(int newDamage) {}
    
    @Override
    public float getDamage() {
        return 0;
    }
    
    
    // ==================================================
 	//                      Visuals
 	// ==================================================
    @Override
    public ResourceLocation getTexture() {
    	return null;
    }
    
    
    // ==================================================
 	//                      Sounds
 	// ==================================================
	@Override
	public String getLaunchSound() {
		return null;
	}
}
