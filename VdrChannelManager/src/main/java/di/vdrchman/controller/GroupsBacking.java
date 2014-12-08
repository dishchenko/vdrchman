package di.vdrchman.controller;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import org.richfaces.event.DataScrollEvent;

import di.vdrchman.data.GroupRepository;
import di.vdrchman.data.GroupsManager;
import di.vdrchman.model.Group;

@Model
public class GroupsBacking {

	@Inject
	private GroupsManager groupsManager;

	@Inject
	private GroupRepository groupRepository;

	// The user is going to add a new group
	public void intendAddGroup() {
		groupsManager.setEditedGroup(new Group());
	}

	// Really adding a new group
	public void doAddGroup() {
		groupRepository.add(groupsManager.getEditedGroup());
		groupsManager.retrieveAllGroups();
		groupsManager.clearCheckedGroups();
		groupsManager.clearGroupCheckboxes();
		groupsManager.turnScrollerPage(groupsManager.getEditedGroup());
	}

	// The user is going to update a group
	public void intendUpdateGroup(Group group) {
		groupsManager.setEditedGroup(new Group(group));
	}

	// Really updating the group
	public void doUpdateGroup() {
		groupRepository.update(groupsManager.getEditedGroup());
		groupsManager.retrieveAllGroups();
		groupsManager.clearCheckedGroups();
		groupsManager.clearGroupCheckboxes();
		groupsManager.turnScrollerPage(groupsManager.getEditedGroup());
	}

	// Going to remove some checked groups
	public void intendRemoveGroups() {
		groupsManager.collectCheckedGroups();
	}

	// Do that removal
	public void doRemoveGroups() {
		for (Group group : groupsManager.getCheckedGroups()) {
			groupRepository.delete(group);
		}
		groupsManager.retrieveAllGroups();
		groupsManager.clearCheckedGroups();
		groupsManager.clearGroupCheckboxes();
	}

	// Let's take the checked group's data on the "clipboard"
	public void takeGroup() {
		groupsManager.collectCheckedGroups();
		groupsManager.setTakenGroup(new Group(groupsManager.getCheckedGroups()
				.get(0)));
		groupsManager.clearCheckedGroups();
		groupsManager.clearGroupCheckboxes();
	}

	// The user's gonna add a new group using data from the "clipboard"
	public void intendCopyGroup() {
		groupsManager.setEditedGroup(new Group(groupsManager.getTakenGroup()));
		groupsManager.getEditedGroup().setId(null);
	}

	// Well this method is called when the user changes the table scroller page
	public void onDataTableScroll(DataScrollEvent event) {
		groupsManager.clearCheckedGroups();
		groupsManager.clearGroupCheckboxes();
	}

}
