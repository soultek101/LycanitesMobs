package lycanite.lycanitesmobs.entity;

import java.lang.reflect.Constructor;
import java.util.HashSet;

import lycanite.lycanitesmobs.Utilities;
import lycanite.lycanitesmobs.api.ICustomProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class EntityLaser extends EntityThrowable implements ICustomProjectile {
	// Properties:
	public EntityLivingBase shootingEntity;
	public EntityLivingBase followEntity; // Used for Eyewig Mount Ability.
	public int shootingEntityRef = -1;
	public int shootingEntityID = 10;
	
	public byte damage = 1;
	public float projectileScale = 1f;
	public float projectileWidth = 0.2f;
	public float projectileHeight = 0.2f;
	
	// Laser:
	public EntityLaserEnd laserEnd;
	public int laserEndRef = -1;
	public int laserEndID = 11;
	
	public int laserTime = 100;
	public int laserDelay = 5;
	public float laserRange;
	public float laserWidth;
	public float laserLength = 0;
	public int laserTimeID = 12;

	// Laser End:
	private double targetX;
	private double targetY;
	private double targetZ;
	
	// Offsets:
	public double offsetX = 0;
	public double offsetY = 0;
	public double offsetZ = 0;
	public int offsetIDStart = 13;
	
    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityLaser(World par1World) {
        super(par1World);
        this.setStats();
        this.setTime(0);
    }

    public EntityLaser(World par1World, double par2, double par4, double par6, int setTime, int setDelay) {
        super(par1World, par2, par4, par6);
        this.laserTime = setTime;
        this.laserDelay = setDelay;
        this.setStats();
    }

    public EntityLaser(World par1World, EntityLivingBase par2EntityLivingBase, int setTime, int setDelay) {
        this(par1World, par2EntityLivingBase, setTime, setDelay, null);
    }

    public EntityLaser(World par1World, EntityLivingBase par2EntityLivingBase, int setTime, int setDelay, EntityLivingBase followEntity) {
        super(par1World, par2EntityLivingBase);
        this.shootingEntity = par2EntityLivingBase;
        this.laserTime = setTime;
        this.laserDelay = setDelay;
        this.setStats();
        this.followEntity = followEntity;
        this.syncOffset();
    }
    
    public void setStats() {
        this.setSize(projectileWidth, projectileHeight);
        this.setRange(16.0F);
        this.setLaserWidth(1.0F);
        this.targetX = this.posX;
        this.targetY = this.posY;
        this.targetZ = this.posZ;
        this.dataWatcher.addObject(this.shootingEntityID, this.shootingEntityRef);
        this.dataWatcher.addObject(this.laserEndID, this.laserEndRef);
        this.dataWatcher.addObject(this.laserTimeID, this.laserTime);
        this.dataWatcher.addObject(this.offsetIDStart, (float)this.offsetX);
        this.dataWatcher.addObject(this.offsetIDStart + 1, (float)this.offsetY);
        this.dataWatcher.addObject(this.offsetIDStart + 2, (float)this.offsetZ);
    }
	
    
    // ==================================================
 	//                   Properties
 	// ==================================================
	public void setOffset(double x, double y, double z) {
		this.offsetX = x;
		this.offsetY = y;
		this.offsetZ = z;
		this.syncOffset();
	}
    
    
    // ==================================================
 	//                   Update
 	// ==================================================
    @Override
    public void onUpdate() {
    	if(!this.worldObj.isRemote) {
    		this.dataWatcher.updateObject(laserTimeID, this.laserTime);
    	}
    	else {
    		this.laserTime = this.dataWatcher.getWatchableObjectInt(this.laserTimeID);
    	}
    	this.syncShootingEntity();
    	
    	//this.syncOffset(); Broken? :(
    	if(!this.worldObj.isRemote && this.shootingEntity != null) {
    		EntityLivingBase entityToFollow = this.shootingEntity;
    		//if(this.followEntity != null)
    			//entityToFollow = this.followEntity;
    		this.posX = entityToFollow.posX + this.offsetX;
    		this.posY = entityToFollow.posY + this.offsetY;
    		this.posZ = entityToFollow.posZ + this.offsetZ;
    	}
    	
    	if(this.laserTime > 0) {
	    	this.updateEnd();
	    	this.laserTime--;
	    	
	    	if(this.laserEnd != null) {
	    		if(this.posX - this.width < this.laserEnd.posX - this.laserEnd.width)
	    			this.boundingBox.minX = this.posX - this.width;
	    		else
	    			this.boundingBox.minX = this.laserEnd.posX - this.laserEnd.width;
	    		
	    		if(this.posX + this.width > this.laserEnd.posX + this.laserEnd.width)
	    			this.boundingBox.maxX = this.posX + this.width;
	    		else
	    			this.boundingBox.maxX = this.laserEnd.posX + this.laserEnd.width;
	    		
	    		
	    		if(this.posY - this.height < this.laserEnd.posY - this.laserEnd.height)
	    			this.boundingBox.minY = this.posY - this.height;
	    		else
	    			this.boundingBox.minY = this.laserEnd.posY - this.laserEnd.height;
	    		
	    		if(this.posY + this.width > this.laserEnd.posY + this.laserEnd.height)
	    			this.boundingBox.maxY = this.posY + this.height;
	    		else
	    			this.boundingBox.maxY = this.laserEnd.posY + this.laserEnd.height;
	    		
	    		
	    		if(this.posZ - this.width < this.laserEnd.posZ - this.laserEnd.width)
	    			this.boundingBox.minZ = this.posZ - this.width;
	    		else
	    			this.boundingBox.minZ = this.laserEnd.posZ - this.laserEnd.width;
	    		
	    		if(this.posZ + this.width > this.laserEnd.posZ + this.laserEnd.width)
	    			this.boundingBox.maxZ = this.posZ + this.width;
	    		else
	    			this.boundingBox.maxZ = this.laserEnd.posZ + this.laserEnd.width;
	    	}
	    	else {
	    		this.boundingBox.minX = this.posX - this.width;
	    		this.boundingBox.maxX = this.posX + this.width;
	    		this.boundingBox.minY = this.posY - this.height;
	    		this.boundingBox.maxY = this.posY + this.height;
	    		this.boundingBox.minZ = this.posZ - this.width;
	    		this.boundingBox.maxZ = this.posZ + this.width;
	    	}
    	}
    	else if(!this.isDead) {
    		this.setDead();
    	}
    }
    
    
    // ==================================================
 	//                   Update End
 	// ==================================================
	public void updateEnd() {
		if(this.worldObj.isRemote) {
			this.laserEndRef = this.dataWatcher.getWatchableObjectInt(this.laserEndID);
			Entity possibleLaserEnd = null;
			if(this.laserEndRef != -1)
				possibleLaserEnd = this.worldObj.getEntityByID(this.laserEndRef);
			if(possibleLaserEnd != null && possibleLaserEnd instanceof EntityLaserEnd)
				this.laserEnd = (EntityLaserEnd)possibleLaserEnd;
			else
				this.laserEnd = null;
			return;
		}
		
		if(this.laserEnd == null)
			fireProjectile();
		
		if(this.laserEnd == null)
			this.laserEndRef = -1;
		else {
			this.laserEndRef = this.laserEnd.entityId;
			
			// Entity Aiming:
			if(this.shootingEntity != null) {
				if(this.shootingEntity instanceof EntityCreatureBase && ((EntityCreatureBase)this.shootingEntity).hasAttackTarget()) {
					EntityLivingBase attackTarget = ((EntityCreatureBase)this.shootingEntity).getAttackTarget();
					this.targetX = attackTarget.posX;
					this.targetY = attackTarget.posY + (attackTarget.height / 2);
					this.targetZ = attackTarget.posZ;
				}
				else {
					Vec3 lookDirection = this.shootingEntity.getLookVec();
					this.targetX = this.shootingEntity.posX + (lookDirection.xCoord * this.laserRange);
					this.targetY = this.shootingEntity.posY + (lookDirection.yCoord * this.laserRange);
					this.targetZ = this.shootingEntity.posZ + (lookDirection.zCoord * this.laserRange);
				}
			}
			
			// Raytracing:
			HashSet<Entity> excludedEntities = new HashSet<Entity>();
			excludedEntities.add(this);
			if(this.shootingEntity != null)
				excludedEntities.add(this.shootingEntity);
			if(this.followEntity != null)
				excludedEntities.add(this.followEntity);
			MovingObjectPosition target = Utilities.raytrace(this.worldObj, this.posX, this.posY, this.posZ, this.targetX, this.targetY, this.targetZ, this.laserWidth, excludedEntities);
			
			// Update Laser End Position:
			double newTargetX = this.targetX;
			double newTargetY = this.targetY;
			double newTargetZ = this.targetZ;
			if(target != null && target.hitVec != null) {
				newTargetX = target.hitVec.xCoord;
				newTargetY = target.hitVec.yCoord;
				newTargetZ = target.hitVec.zCoord;
			}
			this.laserEnd.onUpdateEnd(newTargetX, newTargetY, newTargetZ);
			
			// Damage:
			if(this.laserTime % this.laserDelay == 0 && this.isEntityAlive())
				if(target != null && target.entityHit != null)
					if(this.laserEnd.getDistanceToEntity(target.entityHit) <= (this.laserWidth * 10)) {
						boolean doDamage = true;
						if(target.entityHit instanceof EntityLivingBase) {
							EntityLivingBase owner = this.getThrower();
						    if(this.getThrower() != null && owner instanceof EntityPlayer) {
						    	if(MinecraftForge.EVENT_BUS.post(new AttackEntityEvent((EntityPlayer)owner, target.entityHit))) {
						    		doDamage = false;
						    	}
						    }
						}
						if(doDamage)
							this.updateDamage(target.entityHit);
					}
		}
		
		this.dataWatcher.updateObject(laserEndID, laserEndRef);
		this.playSound(this.getBeamSound(), 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
	}
	
    
    // ==================================================
 	//                    Laser Time
 	// ==================================================
	public void setTime(int time) {
		this.laserTime = time;
	}

	public int getTime() {
		return this.laserTime;
	}
    
    
    // ==================================================
 	//                 Fire Projectile
 	// ==================================================
    public void fireProjectile() {
    	World world = this.worldObj;
    	if(world.isRemote)
    		return;
    	
		try {
			if(this.shootingEntity == null) {
		    	Constructor constructor = getLaserEndClass().getDeclaredConstructor(new Class[] { World.class, double.class, double.class, double.class, EntityLaser.class });
		    	constructor.setAccessible(true);
		    	laserEnd = (EntityLaserEnd)constructor.newInstance(new Object[] { world, this.posX, this.posY, this.posZ, this });
		    }
	        else {
		    	Constructor constructor = getLaserEndClass().getDeclaredConstructor(new Class[] { World.class, EntityLivingBase.class, EntityLaser.class });
		    	constructor.setAccessible(true);
		    	laserEnd = (EntityLaserEnd)constructor.newInstance(new Object[] { world, this.shootingEntity, this });
	        }
	        
	        this.playSound(this.getLaunchSound(), 1.0F, 1.0F / (this.rand.nextFloat() * 0.4F + 0.8F));
	        
	        world.spawnEntityInWorld(laserEnd);
		}
		catch (Exception e) {
			System.out.println("[WARNING] [LycanitesMobs] EntityLaser was unable to instantiate the EntityLaserEnd.");
			e.printStackTrace();
		}
    }
	
    
    // ==================================================
 	//               Sync Shooting Entity
 	// ==================================================
    public void syncShootingEntity() {
    	if(!this.worldObj.isRemote) {
    		if(this.shootingEntity == null) this.shootingEntityRef = -1;
    		else this.shootingEntityRef = this.shootingEntity.entityId;
    		this.dataWatcher.updateObject(this.shootingEntityID, this.shootingEntityRef);
    	}
    	else {
    		this.shootingEntityRef = this.dataWatcher.getWatchableObjectInt(this.shootingEntityID);
    		if(this.shootingEntityRef == -1) this.shootingEntity = null;
    		else {
    			Entity possibleShootingEntity = this.worldObj.getEntityByID(this.shootingEntityRef);
    			if(possibleShootingEntity != null && possibleShootingEntity instanceof EntityLivingBase)
    				this.shootingEntity = (EntityLivingBase)possibleShootingEntity;
    			else
    				this.shootingEntity = null;
    		}
    	}
    }
    
    public void syncOffset() {
    	if(!this.worldObj.isRemote) {
    		this.dataWatcher.updateObject(this.offsetIDStart, (float)this.offsetX);
    		this.dataWatcher.updateObject(this.offsetIDStart + 1, (float)this.offsetY);
    		this.dataWatcher.updateObject(this.offsetIDStart + 2, (float)this.offsetZ);
    	}
    	else {
    		this.offsetX = this.dataWatcher.getWatchableObjectFloat(this.offsetIDStart);
    		this.offsetY = this.dataWatcher.getWatchableObjectFloat(this.offsetIDStart + 1);
    		this.offsetZ = this.dataWatcher.getWatchableObjectFloat(this.offsetIDStart + 2);
    	}
    }
	
    
    // ==================================================
 	//                   Get laser End
 	// ==================================================
    public EntityLaserEnd getLaserEnd() {
        return this.laserEnd;
    }

    public Class getLaserEndClass() {
        return EntityLaserEnd.class;
    }
	
    
    // ==================================================
 	//                    Set Target
 	// ==================================================
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }
	
    
    // ==================================================
 	//                   Movement
 	// ==================================================
    // ========== Gravity ==========
    @Override
    protected float getGravityVelocity() {
        return 0.0F;
    }
    
    
    // ==================================================
 	//                     Impact
 	// ==================================================
    @Override
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition) {
    	return;
    }
    
    
    // ==================================================
 	//                    Collision
 	// ==================================================
    public boolean canBeCollidedWith() {
        return false;
    }
    
    
    // ==================================================
 	//                      Damage
 	// ==================================================
    public void updateDamage(Entity targetEntity) {
    	targetEntity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), (float)damage);
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
 	//                      Stats
 	// ==================================================
    public void setRange(float range) {
    	this.laserRange = range;
    }

    public void setLaserWidth(float width) {
    	this.laserWidth = width;
    }

    public float getLaserWidth() {
    	return this.laserWidth;
    }
    
    
    // ==================================================
 	//                      Visuals
 	// ==================================================
    @Override
    public ResourceLocation getTexture() {
    	return null;
    }
    
    public ResourceLocation getBeamTexture() {
    	return null;
    }
    
    public double[] getLengths() {
    	if(this.laserEnd == null)
    		return new double[] {0.0D, 0.0D, 0.0D};
    	else
    		return new double[] {
    			this.laserEnd.posX - this.posX,
    			this.laserEnd.posY - this.posY,
    			this.laserEnd.posZ - this.posZ
    		};
    }
    
    public float getLength() {
    	if(this.laserEnd == null)
    		return 0;
    	return this.getDistanceToEntity(this.laserEnd);
    }
    
    public float[] getBeamAngles() {
    	float[] angles = new float[] {0, 0, 0, 0};
    	if(this.laserEnd != null) {
    		float dx = (float)(this.laserEnd.posX - this.posX);
    		float dy = (float)(this.laserEnd.posY - this.posY);
    		float dz = (float)(this.laserEnd.posZ - this.posZ);
			angles[0] = (float)Math.toDegrees(Math.atan2(dz, dy)) - 90;
			angles[1] = (float)Math.toDegrees(Math.atan2(dx, dz));
			angles[2] = (float)Math.toDegrees(Math.atan2(dx, dy)) - 90;
			
			// Distance based x/z rotation:
			float dr = (float)Math.sqrt(dx * dx + dz * dz);
			angles[3] = (float)Math.toDegrees(Math.atan2(dr, dy)) - 90;
		}
    	return angles;
    }
    
    
    // ==================================================
 	//                      Sounds
 	// ==================================================
	@Override
	public String getLaunchSound() {
		return null;
	}
	
	public String getBeamSound() {
		return null;
	}
    
    
    // ==================================================
    //                        NBT
    // ==================================================
   	// ========== Read ===========
    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTagCompound) {
    	if(nbtTagCompound.hasKey("LaserTime"))
    		this.setTime(nbtTagCompound.getInteger("LaserTime"));
    	if(nbtTagCompound.hasKey("OffsetX"))
    		this.offsetX = nbtTagCompound.getDouble("OffsetX");
    	if(nbtTagCompound.hasKey("OffsetY"))
    		this.offsetY = nbtTagCompound.getDouble("OffsetY");
    	if(nbtTagCompound.hasKey("OffsetZ"))
    		this.offsetZ = nbtTagCompound.getDouble("OffsetZ");
        super.readEntityFromNBT(nbtTagCompound);
    }
    
    // ========== Write ==========
    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound) {
    	nbtTagCompound.setInteger("LaserTime", this.laserTime);
    	nbtTagCompound.setDouble("OffsetX", this.offsetX);
    	nbtTagCompound.setDouble("OffsetY", this.offsetY);
    	nbtTagCompound.setDouble("OffsetZ", this.offsetZ);
        super.writeEntityToNBT(nbtTagCompound);
    }
}
