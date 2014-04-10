package ca.phon.session.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import antlr.ASdebug.IASDebugStream;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.Orthography;
import ca.phon.session.Comment;
import ca.phon.session.Group;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;
import ca.phon.session.SystemTierType;
import ca.phon.session.Tier;
import ca.phon.session.Word;

/**
 * Basic record implementation
 *
 */
public class RecordImpl implements Record {
	
	/* Attributes */
	private final AtomicReference<Participant> participantRef = new AtomicReference<Participant>();
	
	private volatile boolean excludeFromSearches = false;
	
	private final AtomicReference<UUID> uuidRef = new AtomicReference<UUID>(UUID.randomUUID());
	
	/* default tiers */
	private final Tier<Orthography> orthography;
	
	private final Tier<IPATranscript> ipaTarget;
	
	private final Tier<IPATranscript> ipaActual;
	
	private final Tier<MediaSegment> segment;
	
	private final Tier<String> notes;
	
	private final Tier<PhoneMap> alignment;
	
	/* Additional tiers */
	private final Map<String, Tier<?>> userDefined;

	RecordImpl() {
		super();
		
		final SessionFactory factory = SessionFactory.newFactory();
		orthography = factory.createTier(SystemTierType.Orthography.getName(), Orthography.class, SystemTierType.Orthography.isGrouped());
		ipaTarget = factory.createTier(SystemTierType.IPATarget.getName(), IPATranscript.class, SystemTierType.IPATarget.isGrouped());
		ipaActual = factory.createTier(SystemTierType.IPAActual.getName(), IPATranscript.class, SystemTierType.IPAActual.isGrouped());
		segment = factory.createTier(SystemTierType.Segment.getName(), MediaSegment.class, SystemTierType.Segment.isGrouped());
		notes = factory.createTier(SystemTierType.Notes.getName(), String.class, SystemTierType.Notes.isGrouped());
		alignment = factory.createTier(SystemTierType.SyllableAlignment.getName(), PhoneMap.class, SystemTierType.SyllableAlignment.isGrouped());
		
		userDefined = 
				Collections.synchronizedMap(new HashMap<String, Tier<?>>());
		
		extSupport.initExtensions();
	}
	
	@Override
	public UUID getUuid() {
		return this.uuidRef.get();
	}
	
	@Override
	public void setUuid(UUID id) {
		uuidRef.getAndSet(id);
	}

	@Override
	public Participant getSpeaker() {
		return participantRef.get();
	}

	@Override
	public void setSpeaker(Participant participant) {
		participantRef.getAndSet(participant);
	}

	@Override
	public Tier<MediaSegment> getSegment() {
		return segment;
	}

	@Override
	public void setSegment(Tier<MediaSegment> media) {
		this.segment.removeAll();
		for(int i = 0; i < media.numberOfGroups(); i++) {
			this.segment.addGroup(media.getGroup(i));
		}
	}

	@Override
	public boolean isExcludeFromSearches() {
		return this.excludeFromSearches;
	}

	@Override
	public void setExcludeFromSearches(boolean excluded) {
		this.excludeFromSearches = excluded;
	}

	@Override
	public Tier<Orthography> getOrthography() {
		return orthography;
	}

	@Override
	public void setOrthography(Tier<Orthography> ortho) {
		this.orthography.removeAll();
		for(int i = 0; i < ortho.numberOfGroups(); i++) {
			this.orthography.addGroup(ortho.getGroup(i));
		}
	}

	@Override
	public int numberOfGroups() {
		return this.orthography.numberOfGroups();
	}

	@Override
	public Group getGroup(int idx) {
		if(idx >= 0 && idx < numberOfGroups()) {
			return new GroupImpl(this, idx);
		} else {
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		}
	}

	@Override
	public void removeGroup(int idx) {
		if(idx >= 0 && idx < numberOfGroups()) {
			orthography.removeGroup(idx);
			ipaActual.removeGroup(idx);
			ipaTarget.removeGroup(idx);
			alignment.removeGroup(idx);
			
			for(String tierName:getExtraTierNames()) {
				final Tier<?> tier = getTier(tierName);
				if(tier.isGrouped()) {
					tier.removeGroup(idx);
				}
			}
		} else {
			throw new IndexOutOfBoundsException("Invalid group index " + idx);
		}
	}

//	@Override
//	public void mergeGroups(int startIdx, int endIdx) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public void splitGroup(int groupIdx, int wordIdx) {
//		// TODO Auto-generated method stub
//	}

	@Override
	public Group addGroup() {
		int gidx = orthography.numberOfGroups();
		
		orthography.addGroup(new Orthography());
		ipaTarget.addGroup(new IPATranscript());
		ipaActual.addGroup(new IPATranscript());
		alignment.addGroup(new PhoneMap(ipaTarget.getGroup(gidx), ipaActual.getGroup(gidx)));
		
		for(String tierName:getExtraTierNames()) {
			final Tier<String> tier = getTier(tierName, String.class);
			tier.addGroup("");
		}
		
		if(gidx == 0) {
			notes.addGroup("");
			segment.addGroup(SessionFactory.newFactory().createMediaSegment());
		}
		
		return new GroupImpl(this, gidx);
	}

	@Override
	public Group addGroup(int idx) {
		orthography.addGroup(idx, new Orthography());
		ipaTarget.addGroup(idx, new IPATranscript());
		ipaActual.addGroup(idx, new IPATranscript());
		alignment.addGroup(idx, new PhoneMap(ipaTarget.getGroup(idx), ipaActual.getGroup(idx)));
		
		for(String tierName:getExtraTierNames()) {
			final Tier<String> tier = getTier(tierName, String.class);
			tier.addGroup(idx, "");
		}
		
		return new GroupImpl(this, idx);
	}

	@Override
	public Tier<IPATranscript> getIPATarget() {
		return this.ipaTarget;
	}

	@Override
	public void setIPATarget(Tier<IPATranscript> ipa) {
		this.ipaTarget.removeAll();
		for(int i = 0; i < ipa.numberOfGroups(); i++) {
			this.ipaTarget.addGroup(ipa.getGroup(i));
		}
	}

	@Override
	public Tier<IPATranscript> getIPAActual() {
		return this.ipaActual;
	}

	@Override
	public void setIPAActual(Tier<IPATranscript> ipa) {
		this.ipaActual.removeAll();
		for(int i = 0; i < ipa.numberOfGroups(); i++) {
			this.ipaActual.addGroup(ipa.getGroup(i));
		}
	}
	
	@Override
	public Tier<PhoneMap> getPhoneAlignment() {
		return this.alignment;
	}
	
	@Override
	public void setPhoneAlignment(Tier<PhoneMap> phoneAlignment) {
		this.alignment.removeAll();
		for(int i = 0; i < phoneAlignment.numberOfGroups(); i++) {
			this.alignment.addGroup(phoneAlignment.getGroup(i));
		}
	}

	@Override
	public Tier<String> getNotes() {
		return this.notes;
	}

	@Override
	public void setNotes(Tier<String> notes) {
		this.notes.removeAll();
		if(notes.numberOfGroups() > 0) {
			this.notes.addGroup(notes.getGroup(0));
		}
	}

	@Override
	public Class<?> getTierType(String name) {
		if(SystemTierType.isSystemTier(name)) {
			return SystemTierType.tierFromString(name).getDeclaredType();
		} else {
			for(Tier<?> t:userDefined.values()) {
				if(t.getName().equals(name)) {
					return t.getDeclaredType();
				}
			}
		}
		return null;
	}

	@Override
	public boolean hasTier(String name) {
		return getExtraTierNames().contains(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Tier<T> getTier(String name, Class<T> type) {
		Tier<T> retVal = null;
		
		final SystemTierType systemTierType = SystemTierType.tierFromString(name);
		final Tier<T> systemTier = getSystemTier(systemTierType, type);
		
		if(systemTier != null) {
			retVal = systemTier;
		} else {
			final Tier<?> userTier = userDefined.get(name);
			if(userTier != null) {
				if(userTier.getDeclaredType() == type) {
					retVal = (Tier<T>)userTier;
				} else if(type == String.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					retVal = factory.createTier(name, type, userTier.isGrouped());
					// copy group data as string
					for(int i = 0; i < userTier.numberOfGroups(); i++) {
						final Object obj = userTier.getGroup(i);
						final String val = obj.toString();
						final T tierVal = (T)type.getClass().cast(val);
						retVal.addGroup(tierVal);
					}
				}
			}
		}
		
		return retVal;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Tier<T> getSystemTier(SystemTierType systemTierType, Class<T> type) {
		Tier<T> retVal = null;
		
		Tier<?> systemTier = null;
		if(systemTierType != null) {
			switch(systemTierType) {
			case Orthography:
				systemTier = getOrthography();
				break;
				
			case IPATarget:
				systemTier = getIPATarget();
				break;
				
			case IPAActual:
				systemTier = getIPAActual();
				break;
				
			case SyllableAlignment:
				systemTier = getPhoneAlignment();
				break;
				
			case Segment:
				systemTier = getSegment();
				break;
				
			case Notes:
				systemTier = getNotes();
				break;
				
			default:
				break;	
			}
			if(systemTier != null) {
				if(systemTier.getDeclaredType() == type) {
					retVal = (Tier<T>)systemTier;
				} else if(type == String.class) {
					// create a new string tier to return
					final SessionFactory factory = SessionFactory.newFactory();
					retVal = factory.createTier(systemTier.getName(), type, systemTier.isGrouped());
					// copy group data as string
					for(int i = 0; i < systemTier.numberOfGroups(); i++) {
						final Object obj = systemTier.getGroup(i);
						final String val = obj.toString();
//						final T tierVal = (T)type.getClass().cast(val);
						retVal.addGroup((T)val);
					}
				}
			}
		}
		
		return retVal;
	}
	
	
	@Override
	public Tier<?> getTier(String name) {
		return getTier(name, getTierType(name));
	}

	@Override
	public Set<String> getExtraTierNames() {
		return userDefined.keySet();
	}

	@Override
	public void removeTier(String name) {
		userDefined.remove(name);
	}
	
	// COMMENTS
	private final List<Comment> comments = 
			Collections.synchronizedList(new ArrayList<Comment>());
	
	@Override
	public int getNumberOfComments() {
		return comments.size();
	}

	@Override
	public Comment getComment(int idx) {
		return comments.get(idx);
	}

	@Override
	public void addComment(Comment comment) {
		comments.add(comment);
	}

	@Override
	public void removeComment(Comment comment) {
		comments.remove(comment);
	}

	@Override
	public void removeComment(int idx) {
		comments.remove(idx);
	}
	
	/* Extension support */
	private ExtensionSupport extSupport = new ExtensionSupport(Record.class, this);
	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public void putTier(Tier<?> tier) {
		userDefined.put(tier.getName(), tier);
	}

	@Override
	public int mergeGroups(int grp1, int grp2) {
		if(grp2 <= grp1) {
			throw new IllegalArgumentException("grp2 must be greater than grp1");
		}
		if(grp2 - grp1 != 1) {
			throw new IllegalArgumentException("groups must be adjacent to merge");
		}
		if(grp1 < 0 || grp1 >= numberOfGroups()) {
			throw new ArrayIndexOutOfBoundsException(grp1);
		}
		if(grp2 < 0 || grp2 >= numberOfGroups()) {
			throw new ArrayIndexOutOfBoundsException(grp2);
		}
		
		final Group group1 = getGroup(grp1);
		final Group group2 = getGroup(grp2);
		int retVal = group1.getAlignedWordCount();
		
		// orthography
		final Orthography ortho = new Orthography();
		ortho.addAll(group1.getOrthography());
		ortho.addAll(group2.getOrthography());
		group1.setOrthography(ortho);
		
		// ipa target
		final IPATranscriptBuilder tBuilder = new IPATranscriptBuilder();
		tBuilder.append(group1.getIPATarget());
		if(tBuilder.size() > 0) tBuilder.appendWordBoundary();
		tBuilder.append(group2.getIPATarget());
		final IPATranscript ipaTarget = tBuilder.toIPATranscript();
		group1.setIPATarget(ipaTarget);
		
		final IPATranscriptBuilder aBuilder = new IPATranscriptBuilder();
		aBuilder.append(group1.getIPAActual());
		if(aBuilder.size() > 0) aBuilder.appendWordBoundary();
		aBuilder.append(group2.getIPAActual());
		final IPATranscript ipaActual = aBuilder.toIPATranscript();
		group1.setIPAActual(ipaActual);
		
		// TODO alignment
		
		// other tiers
		for(String tierName:getExtraTierNames()) {
			final String tierVal = group1.getTier(tierName, String.class);
			if(tierVal != null) {
				final String newVal = tierVal + " " + group2.getTier(tierName, String.class);
				group1.setTier(tierName, String.class, newVal);
			}
		}
		
		removeGroup(grp2);
		
		return retVal;
	}
	
	@Override
	public Group splitGroup(int grp, int wrd) {
		if(grp < 0 || grp >= numberOfGroups()) {
			throw new ArrayIndexOutOfBoundsException(grp);
		}
		final Group group = getGroup(grp);
		if(wrd < 0 || wrd >= group.getAlignedWordCount()) {
			throw new ArrayIndexOutOfBoundsException(wrd);
		}
		final Word word = group.getAlignedWord(wrd);
		
		final Group newGroup = addGroup(grp+1);
		
		// orthography
		final OrthoElement ele = word.getOrthography();
		int wordIdx = group.getOrthography().indexOf(ele);
		final Orthography ortho = new Orthography(group.getOrthography().subList(0, wordIdx));
		final Orthography newOrtho = new Orthography(group.getOrthography().subList(wordIdx, group.getOrthography().size()));
		group.setOrthography(ortho);
		newGroup.setOrthography(newOrtho);
		
		// ipa target
		final IPATranscript ipaT = word.getIPATarget();
		int ipaTIdx = group.getIPATarget().indexOf(ipaT);
		IPATranscript ipaTarget = group.getIPATarget();
		IPATranscript newIpaTarget = new IPATranscript();
		if(ipaTIdx >= 0) {
			ipaTarget = group.getIPATarget().subsection(0, ipaTIdx-1);
			newIpaTarget = group.getIPATarget().subsection(ipaTIdx, group.getIPATarget().length());
		}
		group.setIPATarget(ipaTarget);
		newGroup.setIPATarget(newIpaTarget);
		
		// ipa actual
		final IPATranscript ipaA = word.getIPAActual();
		int ipaAIdx = group.getIPAActual().indexOf(ipaA);
		IPATranscript ipaActual = group.getIPAActual();
		IPATranscript newIpaActual = new IPATranscript();
		if(ipaAIdx >= 0) {
			ipaActual = group.getIPAActual().subsection(0, ipaAIdx-1);
			newIpaActual = group.getIPAActual().subsection(ipaAIdx, group.getIPAActual().length());
		}
		group.setIPAActual(ipaActual);
		newGroup.setIPAActual(newIpaActual);

		// TODO alignment
		
		// other tiers
		for(String tierName:getExtraTierNames()) {
			final String tierVal = group.getTier(tierName, String.class);
			if(tierVal != null) {
				final String words[] = tierVal.split("\\p{Space}");
				
				String val = "";
				String newVal = "";
				for(int i = 0; i < words.length; i++) {
					if(i < wrd) {
						val += (val.length() > 0 ? " " : "") + words[i];
					} else {
						newVal += (newVal.length() > 0 ? " " : "") + words[i];
					}
				}
				group.setTier(tierName, String.class, val);
				newGroup.setTier(tierName, String.class, newVal);
			}
		}
		
		return newGroup;
	}
}
