package catering.businesslogic.holiday;

import java.sql.Date;
import java.util.ArrayList;

import catering.businesslogic.CatERing;
import catering.businesslogic.UseCaseLogicException;
import catering.businesslogic.staff.Staff;
import catering.businesslogic.user.User;

public class HolidayManager {

    private ArrayList<HolidayEventReceiver> eventReceivers;

    public HolidayManager() {
        this.eventReceivers = new ArrayList<>();
    }

    public void addEventReceiver(HolidayEventReceiver rec) {
        this.eventReceivers.add(rec);
    }

    public boolean isStaffOnHoliday(Staff s, Date date) {
        if (s == null || date == null) return false;
        return HolidayRequest.hasApprovedHoliday(s.getId(), date);
    }

    public HolidayRequest createRequest(Staff s, Date start, Date end) {
        HolidayRequest hr = new HolidayRequest(s, start, end);
        HolidayRequest.create(hr);
        notifyHolidayCreated(hr);
        return hr;
    }

    public void manageHolidayRequest(HolidayRequest hr, String status) throws UseCaseLogicException {
        User user = CatERing.getInstance().getUserManager().getCurrentUser();
        if (user == null || !user.isOwner()) {
            throw new UseCaseLogicException("Solo il proprietario può gestire le ferie");
        }
        hr.setStatus(status);
        HolidayRequest.updateStatus(hr);
        notifyHolidayManaged(hr);
    }

    private void notifyHolidayCreated(HolidayRequest hr) {
        for (HolidayEventReceiver er : eventReceivers) {
            er.updateHolidayCreated(hr);
        }
    }

    private void notifyHolidayManaged(HolidayRequest hr) {
        for (HolidayEventReceiver er : eventReceivers) {
            er.updateHolidayManaged(hr);
        }
    }
}