package net.redboltmedia.witchercraft.procedures;

import net.redboltmedia.witchercraft.network.WitchercraftModVariables;
import net.redboltmedia.witchercraft.init.WitchercraftModAttributes;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;

public class ApplyMutagenBonusProcedure {
	public static void execute(Entity entity) {
		if (entity == null)
			return;
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g1_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g1_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g1_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g1_s3"));
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket1 == 1) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket1 == 2) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_base"), 2, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s1"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s2"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s3"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket1 == 3) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket1 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket2 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket3 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g1_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g2_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g2_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g2_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g2_s3"));
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket2 == 1) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket2 == 2) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_base"), 2, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s1"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s2"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s3"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket2 == 3) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket4 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket5 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket6 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g2_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g3_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g3_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g3_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g3_s3"));
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket3 == 1) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket3 == 2) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_base"), 2, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s1"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s2"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s3"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket3 == 3) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket7 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket8 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket9 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g3_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g4_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g4_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g4_base"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s1"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s2"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(Attributes.MAX_HEALTH).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s3"));
		}
		if (entity instanceof LivingEntity _entity) {
			_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).removeModifier(Identifier.parse("witchercraft:mutagen_g4_s3"));
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket4 == 1) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 >= 100 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 < 200) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.INCREASED_DAMAGE).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket4 == 2) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_base"), 2, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
					_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s1"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s2"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 >= 200 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 < 300) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s3"), 1, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(Attributes.MAX_HEALTH).hasModifier(modifier.id())) {
						_entity.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(modifier);
					}
				}
			}
		}
		if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftMutagenSocket4 == 3) {
			if (entity instanceof LivingEntity _entity) {
				AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_base"), 10, AttributeModifier.Operation.ADD_VALUE);
				if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
					_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket10 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s1"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket11 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s2"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
			if (entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 >= 300 && entity.getData(WitchercraftModVariables.PLAYER_VARIABLES).witchercraftPerkSocket12 < 400) {
				if (entity instanceof LivingEntity _entity) {
					AttributeModifier modifier = new AttributeModifier(Identifier.parse("witchercraft:mutagen_g4_s3"), 5, AttributeModifier.Operation.ADD_VALUE);
					if (!_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).hasModifier(modifier.id())) {
						_entity.getAttribute(WitchercraftModAttributes.SIGN_INTENSITY).addTransientModifier(modifier);
					}
				}
			}
		}
	}
}