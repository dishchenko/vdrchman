package di.vdrchman.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import di.vdrchman.model.Group;

@SessionScoped
@Named
public class GroupsManager implements Serializable {

	private static final long serialVersionUID = -993964978044396131L;

	@Inject
	private GroupRepository groupRepository;

	// Group list for the current application user
	private List<Group> groups;

	// Number of table rows per page
	private final int rowsPerPage = 10;
	// Current table scroller page
	private int scrollerPage = 1;

	// Map of group IDs and checked checkboxes
	private Map<Long, Boolean> groupCheckboxes = new HashMap<Long, Boolean>();
	// List of checked groups built on checkboxes map
	private List<Group> checkedGroups = new ArrayList<Group>();

	// The group which the user is going to add/update
	private Group editedGroup = new Group();

	// The "clipboard": the place to store the group taken by user
	private Group takenGroup = null;

	// Fill in checkedGroups list with groups corresponding to checkboxes
	// checked in the data table on the page
	public void collectCheckedGroups() {
		clearCheckedGroups();

		for (Group group : groups) {
			if (groupCheckboxes.get(group.getId()) != null) {
				if (groupCheckboxes.get(group.getId())) {
					checkedGroups.add(group);
				}
			}
		}
	}

	// Clear the list of checked groups
	public void clearCheckedGroups() {
		checkedGroups.clear();
	}

	// Clear the map of group checkboxes
	public void clearGroupCheckboxes() {
		groupCheckboxes.clear();
	}

	// Find and set the table scroller page to show the group given
	public void turnScrollerPage(Group group) {
		List<Group> groups;
		int i;

		groups = groupRepository.findAll();
		i = 0;
		for (Group theGroup : groups) {
			if (theGroup.getId().equals(group.getId())) {
				scrollerPage = i / rowsPerPage + 1;
				break;
			}
			++i;
		}
	}

	// Set scroller page value to the last page if it is beyond now.
	public void adjustLastScrollerPage() {
		int maxPageNo;

		maxPageNo = (groups.size() - 1) / rowsPerPage + 1;
		if (scrollerPage > maxPageNo) {
			scrollerPage = maxPageNo;
		}
	}

	// (Re)Fill in the group list
	@PostConstruct
	public void retrieveAllGroups() {
		groups = groupRepository.findAll();
	}

	public List<Group> getGroups() {

		return groups;
	}

	public int getRowsPerPage() {

		return rowsPerPage;
	}

	public int getScrollerPage() {

		return scrollerPage;
	}

	public void setScrollerPage(int scrollerPage) {
		this.scrollerPage = scrollerPage;
	}

	public Map<Long, Boolean> getGroupCheckboxes() {

		return groupCheckboxes;
	}

	public List<Group> getCheckedGroups() {

		return checkedGroups;
	}

	public Group getEditedGroup() {

		return editedGroup;
	}

	public void setEditedGroup(Group editedGroup) {
		this.editedGroup = editedGroup;
	}

	public Group getTakenGroup() {

		return takenGroup;
	}

	public void setTakenGroup(Group takenGroup) {
		this.takenGroup = takenGroup;
	}

}
