/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.history;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.ClipboardManager;
import android.view.*;
import android.widget.*;
import com.melnykov.fab.FloatingActionButton;
import org.solovyev.android.calculator.*;
import org.solovyev.android.calculator.jscl.JsclOperation;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static android.view.Menu.NONE;
import static org.solovyev.android.calculator.CalculatorEventType.clear_history_requested;

public abstract class BaseHistoryFragment extends ListFragment implements CalculatorEventListener {

    @Nonnull
    private final DialogInterface.OnClickListener clearDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    clearHistory();
                    break;
            }
        }
    };
    @Nonnull
    private HistoryArrayAdapter adapter;
    @Nonnull
    private FragmentUi ui;
    @Nullable
    private AlertDialog clearDialog;

    protected BaseHistoryFragment(@Nonnull CalculatorFragmentType fragmentType) {
        ui = new FragmentUi(fragmentType.getDefaultLayoutId(), fragmentType.getDefaultTitleResId(), false);
    }

    @Nonnull
    public static String getHistoryText(@Nonnull HistoryState state) {
        return state.editor.getTextString() + getIdentitySign(state.display.getOperation()) + state.display.getText();
    }

    @Nonnull
    private static String getIdentitySign(@Nonnull JsclOperation operation) {
        return operation == JsclOperation.simplify ? "≡" : "=";
    }

    public void useState(@Nonnull final HistoryState state) {
        App.getEditor().setState(state.editor);
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof CalculatorActivity)) {
            activity.finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ui.onCreate(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return ui.onCreateView(this, inflater, container);
    }

    @Override
    public void onViewCreated(View root, Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        ui.onViewCreated(this, root);

        adapter = new HistoryArrayAdapter(this.getActivity(), getItemLayoutId(), R.id.history_item, new ArrayList<HistoryState>());
        setListAdapter(adapter);

        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        final FloatingActionButton fab = (FloatingActionButton) root.findViewById(R.id.fab);
        fab.attachToListView(lv);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Locator.getInstance().getCalculator().fireCalculatorEvent(clear_history_requested, null);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(final AdapterView<?> parent,
                                    final View view,
                                    final int position,
                                    final long id) {
                useState((HistoryState) parent.getItemAtPosition(position));
            }
        });

        registerForContextMenu(lv);
    }

    @Override
    public void onResume() {
        super.onResume();

        this.ui.onResume(this);

        updateAdapter();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        final HistoryState state = (HistoryState) getListView().getItemAtPosition(info.position);

        // todo serso: fix
        if (true) {
            menu.add(NONE, R.string.c_use, NONE, R.string.c_use);
            menu.add(NONE, R.string.c_copy_expression, NONE, R.string.c_copy_expression);
            if (shouldHaveCopyResult(state)) {
                menu.add(NONE, R.string.c_copy_result, NONE, R.string.c_copy_result);
            }
            menu.add(NONE, R.string.c_edit, NONE, R.string.c_edit);
            menu.add(NONE, R.string.c_remove, NONE, R.string.c_remove);
        } else {
            menu.add(NONE, R.string.c_use, NONE, R.string.c_use);
            menu.add(NONE, R.string.c_copy_expression, NONE, R.string.c_copy_expression);
            if (shouldHaveCopyResult(state)) {
                menu.add(NONE, R.string.c_copy_result, NONE, R.string.c_copy_result);
            }
            menu.add(NONE, R.string.c_save, NONE, R.string.c_save);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final Context context = getActivity();
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final HistoryState state = (HistoryState) getListView().getItemAtPosition(info.position);

        switch (item.getItemId()) {
            case R.string.c_use:
                useState(state);
                return true;
            case R.string.c_copy_expression:
                final String editorText = state.editor.getTextString();
                if (!Strings.isEmpty(editorText)) {
                    final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
                    clipboard.setText(editorText);
                    Toast.makeText(context, context.getText(R.string.c_expression_copied), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.string.c_copy_result:
                final String displayText = state.display.getText();
                if (!Strings.isEmpty(displayText)) {
                    final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Activity.CLIPBOARD_SERVICE);
                    clipboard.setText(displayText);
                    Toast.makeText(context, context.getText(R.string.c_result_copied), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.string.c_save:
                createEditHistoryDialog(state, context, true);
                return true;
            case R.string.c_edit:
                createEditHistoryDialog(state, context, false);
                return true;
            case R.string.c_remove:
                getAdapter().remove(state);
                Locator.getInstance().getHistory().removeSavedHistory(state);
                Toast.makeText(context, context.getText(R.string.c_history_was_removed), Toast.LENGTH_LONG).show();
                getAdapter().notifyDataSetChanged();
                return true;

        }
        return super.onContextItemSelected(item);
    }

    private void createEditHistoryDialog(@Nonnull final HistoryState state, @Nonnull final Context context, final boolean save) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View editView = layoutInflater.inflate(R.layout.history_edit, null);
        final TextView historyExpression = (TextView) editView.findViewById(R.id.history_edit_expression);
        historyExpression.setText(BaseHistoryFragment.getHistoryText(state));

        final EditText comment = (EditText) editView.findViewById(R.id.history_edit_comment);
        comment.setText(state.getComment());

        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(save ? R.string.c_save_history : R.string.c_edit_history)
                .setCancelable(true)
                .setNegativeButton(R.string.c_cancel, null)
                .setPositiveButton(R.string.c_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*if (save) {
                            final HistoryState savedHistoryItem = Locator.getInstance().getHistory().addSavedState(state);
                            savedHistoryItem.setComment(comment.getText().toString());
                            Locator.getInstance().getHistory().save();
                            // we don't need to add element to the adapter as adapter of another activity must be updated and not this
                            //data.getAdapter().add(savedHistoryItem);
                        } else {
                            state.setComment(comment.getText().toString());
                            Locator.getInstance().getHistory().save();
                        }
                        getAdapter().notifyDataSetChanged();*/
                        Toast.makeText(context, context.getText(R.string.c_history_saved), Toast.LENGTH_LONG).show();
                    }
                })
                .setView(editView);

        builder.create().show();
    }

    private boolean shouldHaveCopyResult(@Nonnull HistoryState state) {
        return !state.display.isValid() || !Strings.isEmpty(state.display.getText());
    }

    @Override
    public void onPause() {
        ui.onPause(this);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        ui.onDestroyView(this);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (clearDialog != null) {
            clearDialog.dismiss();
            clearDialog = null;
        }
        ui.onDestroy(this);

        super.onDestroy();
    }

    protected abstract int getItemLayoutId();

    private void updateAdapter() {
        final List<HistoryState> historyList = getHistoryItems();

        final ArrayAdapter<HistoryState> adapter = getAdapter();
        try {
            adapter.setNotifyOnChange(false);
            adapter.clear();
            for (HistoryState historyState : historyList) {
                adapter.add(historyState);
            }
        } finally {
            adapter.setNotifyOnChange(true);
        }

        adapter.notifyDataSetChanged();
    }

    @Nonnull
    protected abstract List<HistoryState> getHistoryItems();

    protected abstract void clearHistory();

    @Nonnull
    protected HistoryArrayAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onCalculatorEvent(@Nonnull CalculatorEventData calculatorEventData, @Nonnull CalculatorEventType calculatorEventType, @Nullable Object data) {
        switch (calculatorEventType) {
            case history_state_added:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateAdapter();
                    }
                });
                break;
            case clear_history_requested:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clearDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.cpp_clear_history_title)
                                .setMessage(R.string.cpp_clear_history_message)
                                .setPositiveButton(R.string.cpp_clear_history, clearDialogListener)
                                .setNegativeButton(R.string.c_cancel, clearDialogListener)
                                .create();
                        clearDialog.show();
                    }
                });
                break;
        }

    }
}