package data.representation.actionbased.messages.email;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.mail.MessagingException;

import data.representation.actionbased.messages.ComparableAddress;

public class JavaMailEmailThread<Message extends JavaMailEmailMessage> extends EmailThread<ComparableAddress, Message>{
	
	private Long responseTime = null;
	private boolean searchedForResponseTime = false;
	
	private Set<ComparableAddress> addresses = null;
	
	private String subject = null;
	private boolean searchedForSubject = false;
	
	public JavaMailEmailThread() {
	}
	
	public String getSubject() {
		if (!searchedForSubject && getThreadedActions().size() > 0) {
			try {
				subject = getThreadedActions().iterator().next().getBaseSubject();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			searchedForSubject = true;
		}
		return subject;
	}
	
	public Long getResponseTime() throws MessagingException {
		if (!searchedForResponseTime) {
			if (getThreadedActions().size() < 2) {
				return null;
			}

			Date[] orignalAndResponseTime = getFirstAndResponseDates();

			if (orignalAndResponseTime[0] != null && orignalAndResponseTime[1] != null) {
				responseTime = orignalAndResponseTime[1].getTime() - orignalAndResponseTime[0].getTime();
			}
			searchedForResponseTime = true;
		}
		return responseTime;
	}
	
	public Set<ComparableAddress> getAddresses() throws MessagingException {
		if (addresses == null) {
			addresses = new HashSet<ComparableAddress>();
			for (JavaMailEmailMessage message : getThreadedActions()) {
				if (message.getCollaborators() != null) {
					addresses.addAll(message.getCollaborators());
				}
			}
		}
		return addresses;
	}
	
}
