package es.udc.psi14.prueba;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by 4m1g0 on 13/01/15.
 */
public class SensorListAdapter extends BaseAdapter {

    Context context;
    ArrayList<Map<String, String>> data;
    private static LayoutInflater inflater = null;

    public SensorListAdapter(Context context, ArrayList<Map<String, String>> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.sensor_row, null);
        TextView sensor_name = (TextView) vi.findViewById(R.id.tv_sensor_name);
        sensor_name.setText(data.get(position).get("name"));
        TextView sensor_value = (TextView) vi.findViewById(R.id.tv_sensor_value);
        sensor_value.setText(data.get(position).get("value"));
        return vi;
    }
}
