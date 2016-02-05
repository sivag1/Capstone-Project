package com.sivag1.juiceboard.data;

/**
 * Created by sivag1 on 1/31/16.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;
import nl.qbusict.cupboard.CupboardFactory;
import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.EntityConverterFactory;
import nl.qbusict.cupboard.convert.ReflectiveEntityConverter;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class MyDBHelper extends SQLiteOpenHelper
{

    private static final String DATABASE_NAME = "juicelevel.db";
    private static final int VERSION = 1;

    static {
        // As an example, a custom instance of Cupboard is set as the global instance.
        // A custom instance is needed if you want Cupboard to use annotations or if you require custom converters.

        CupboardFactory.setCupboard(new CupboardBuilder().useAnnotations().registerEntityConverterFactory(new EntityConverterFactory() {
            @Override
            public <T> EntityConverter<T> create(Cupboard cupboard, Class<T> type) {
                if (type == JuiceLevel.class) {
                    EntityConverter<JuiceLevel> delegate = new ReflectiveEntityConverter<JuiceLevel>(cupboard, JuiceLevel.class) {
                        @Override
                        protected boolean isIgnored(Field field) {
                            return super.isIgnored(field);
                        }

                        @Override
                        public JuiceLevel fromCursor(Cursor cursor) {
                            JuiceLevel juiceLevel = super.fromCursor(cursor);
                            return juiceLevel;
                        }
                    };
                    return (EntityConverter<T>) delegate;
                }
                return null;
            }
        }).build());

        // Then the models are registered with cupboard. A model should be registered before it can be
        // used in any way. There are a few options to make sure the models are registered:
        // 1. In a static block like this in a SQLiteOpenHelper or ContentProvider
        // 2. In a custom Application class either form a static block or onCreate
        // 3. By creating your own factory class and have the static block there.

        cupboard().register(JuiceLevel.class);
    }

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create tables won't upgrade tables, unlike upgradeTables() below.
        cupboard().withDatabase(db).createTables();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        cupboard().withDatabase(db).upgradeTables();
    }
}
