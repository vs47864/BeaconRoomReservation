package de.iosb.fraunhofer.baeconroomreservation.activity.admin;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.view.MotionEvent;
import android.view.View;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.alamkanak.weekview.WeekViewLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.iosb.fraunhofer.baeconroomreservation.Constants;
import de.iosb.fraunhofer.baeconroomreservation.R;
import de.iosb.fraunhofer.baeconroomreservation.activity.BasicActivity;
import de.iosb.fraunhofer.baeconroomreservation.activity.RoomReservationActivity;
import de.iosb.fraunhofer.baeconroomreservation.adapters.Event;
import de.iosb.fraunhofer.baeconroomreservation.entity.RoomDetailsRepresentation;
import de.iosb.fraunhofer.baeconroomreservation.entity.Term;
import de.iosb.fraunhofer.baeconroomreservation.rest.Communicator;

/**
 * This is an activity which is used for displaying events for selected room.
 *
 * @author Viseslav Sako
 */
public class RoomStatusCalendarActivity extends BasicActivity implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener, WeekViewLoader
{
    @BindView(R.id.fab) FloatingActionButton _fab;
    @BindView(R.id.weekView) WeekView weekView;
    private List<Term> terms = new ArrayList<>();
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        countDownTimer = new CountDownTimer(Constants.timeout, Constants.countdownInterval) {
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {finish();}
        }.start();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_calendar);
        ButterKnife.bind(this);

        weekView.setOnEventClickListener(this);
        weekView.setMonthChangeListener(this);
        weekView.setEventLongPressListener(this);
        weekView.setEmptyViewLongPressListener(this);
        weekView.setWeekViewLoader(this);
        weekView.goToHour(new Date().getHours());

        Communicator.getRoomDetails(getIntent().getStringExtra(Constants.roomID), this);

        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                Intent intent = new Intent(getApplicationContext(), RoomReservationActivity.class);
                intent.putExtra(Constants.authorize, true);
                intent.putExtra(Constants.roomID, getIntent().getStringExtra(Constants.roomID));
                intent.putExtra(Constants.roomName, getIntent().getStringExtra(Constants.roomName));
                startActivity(intent);
            }
        });
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> weekViewEvents = new ArrayList<>();
        return weekViewEvents;
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {

    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect)
    {
        countDownTimer.cancel();
        Communicator.setContext(this);
        Communicator.getTermDetails(((Event) event).getTerm());
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public double toWeekViewPeriodIndex(Calendar instance) {
        return 0;
    }

    @Override
    public List<? extends WeekViewEvent> onLoad(int periodIndex) {
        List<Event> weekViewEvents = new ArrayList<>();
        Resources res = getResources();
        TypedArray colorsRes = res.obtainTypedArray(R.array.calendarColors);
        Map<String, Integer> colors = new HashMap<>();
        if(periodIndex == 0){
            int i = 0;

            for (Term term : terms)
            {
                Calendar startTime = Calendar.getInstance();
                Calendar endTime = (Calendar) startTime.clone();
                startTime.setTime(term.getStartDate());
                endTime.setTime(term.getEndDate());
                Event weekViewEvent = new Event(i, term.getDescription(), term.getLocation(), startTime, endTime, term);
                if(colors.containsKey(term.getLocation()))
                {
                    weekViewEvent.setColor(colors.get(term.getLocation()));
                }else
                {
                    weekViewEvent.setColor(colorsRes.getColor(i, 0));
                    colors.put(term.getLocation(), colorsRes.getColor(i, 0));
                    i++;
                }
                weekViewEvents.add(weekViewEvent);
            }
        }
        return weekViewEvents;
    }

    @Override
    public void setDetails(RoomDetailsRepresentation room)
    {
        this.terms = room.getTerms();
        weekView.notifyDatasetChanged();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            countDownTimer.cancel();
            countDownTimer.start();
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        countDownTimer.start();
        Communicator.getRoomDetails(getIntent().getStringExtra(Constants.roomID), this);
    }
}
