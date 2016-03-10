package org.openforis.sepal.component.datasearch.metadata

import groovy.sql.Sql
import org.openforis.sepal.transaction.SqlConnectionManager

interface UsgsDataRepository {

    def getSceneMetadata(dataSetId, sceneId)

    def storeMetadata(dataSetId, Map metadata)

    def updateMetadata(rowId, Map metadata)

}


class JDBCUsgsDataRepository implements UsgsDataRepository {

    private final SqlConnectionManager connectionProvider

    JDBCUsgsDataRepository(SqlConnectionManager connectionProvider) {
        this.connectionProvider = connectionProvider
    }

    @Override
    def getSceneMetadata(dataSetId, sceneId) {
        sql.firstRow("SELECT * FROM usgs_data_repo WHERE sceneID = ? AND dataset_id = ?", [sceneId, dataSetId])
    }

    @Override
    def storeMetadata(dataSetId, Map metadata) {
        sql.executeInsert('''
            INSERT INTO usgs_data_repo (dataset_id,browseAvailable,browseURL,sceneID,sensor,acquisitionDate,
            dateUpdated,path,row,upperLeftCornerLatitude,upperLeftCornerLongitude,upperRightCornerLatitude,
            upperRightCornerLongitude,lowerLeftCornerLatitude,lowerLeftCornerLongitude,lowerRightCornerLatitude,
            lowerRightCornerLongitude,sceneCenterLatitude,sceneCenterLongitude,cloudCover,cloudCoverFull,
            dayOrNight,sunElevation,sunAzimuth,receivingStation,sceneStartTime,sceneStopTime,imageQuality1,
            DATA_TYPE_L1,cartURL,GEOMETRIC_RMSE_MODEL_X,GEOMETRIC_RMSE_MODEL_Y,FULL_PARTIAL_SCENE, geometry)
             VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,ST_GEOMFROMTEXT(?))
             ''',
                [dataSetId, metadata.browseAvailable, metadata.browseURL, metadata.sceneID, metadata.sensor,
                        metadata.acquisitionDate, metadata.dateUpdated, metadata.path, metadata.row, metadata.upperLeftCornerLatitude,
                        metadata.upperLeftCornerLongitude, metadata.upperRightCornerLatitude, metadata.upperRightCornerLongitude,
                        metadata.lowerLeftCornerLatitude, metadata.lowerLeftCornerLongitude, metadata.lowerRightCornerLatitude,
                        metadata.lowerRightCornerLongitude, metadata.sceneCenterLatitude, metadata.sceneCenterLongitude,
                        metadata.cloudCover, metadata.cloudCoverFull, metadata.dayOrNight, metadata.sunElevation, metadata.sunAzimuth,
                        metadata.receivingStation, metadata.sceneStartTime, metadata.sceneStopTime, metadata.imageQuality1, metadata.DATA_TYPE_L1,
                        metadata.cartURL, metadata.GEOMETRIC_RMSE_MODEL_X, metadata.GEOMETRIC_RMSE_MODEL_Y, metadata.FULL_PARTIAL_SCENE,
                        polygon(metadata)
                ])
    }

    @Override
    def updateMetadata(id, Map metadata) {
        sql.executeUpdate('''
            UPDATE usgs_data_repo SET browseAvailable = ?, browseURL =?, sensor = ?,  dateUpdated =?,
            path = ?, row = ?,upperLeftCornerLatitude =?, upperLeftCornerLongitude =?,
            upperRightCornerLatitude = ?, upperRightCornerLongitude = ?, lowerLeftCornerLatitude = ?,
            lowerLeftCornerLongitude = ?, lowerRightCornerLatitude = ?, lowerRightCornerLongitude = ?,
            sceneCenterLatitude = ?, sceneCenterLongitude = ?, cloudCover = ?, cloudCoverFull = ?,
            dayOrNight = ?, sunElevation = ?, sunAzimuth = ?, receivingStation = ?, sceneStartTime = ?,
            sceneStopTime = ?, imageQuality1 = ?, DATA_TYPE_L1 = ?, cartURL = ?, GEOMETRIC_RMSE_MODEL_X = ?,
            GEOMETRIC_RMSE_MODEL_Y = ?, FULL_PARTIAL_SCENE = ?, geometry = ST_GEOMFROMTEXT(?) WHERE id = ?
            ''',
                [metadata.browseAvailable, metadata.browseURL, metadata.sensor, metadata.dateUpdated, metadata.path,
                        metadata.row, metadata.upperLeftCornerLatitude, metadata.upperLeftCornerLongitude,
                        metadata.upperRightCornerLatitude, metadata.upperRightCornerLongitude, metadata.lowerLeftCornerLatitude,
                        metadata.lowerLeftCornerLongitude, metadata.lowerRightCornerLatitude, metadata.lowerRightCornerLongitude,
                        metadata.sceneCenterLatitude, metadata.sceneCenterLongitude, metadata.cloudCover, metadata.cloudCoverFull,
                        metadata.dayOrNight, metadata.sunElevation, metadata.sunAzimuth, metadata.receivingStation,
                        metadata.sceneStartTime, metadata.sceneStopTime, metadata.imageQuality1, metadata.DATA_TYPE_L1,
                        metadata.cartURL, metadata.GEOMETRIC_RMSE_MODEL_X, metadata.GEOMETRIC_RMSE_MODEL_Y, metadata.FULL_PARTIAL_SCENE,
                        polygon(metadata), id
                ])
    }

    private String polygon(Map metadata) {
        metadata.with {
            return "POLYGON((" +
                    "$upperLeftCornerLatitude $upperLeftCornerLongitude, " +
                    "$upperRightCornerLatitude $upperRightCornerLongitude, " +
                    "$lowerRightCornerLatitude $lowerRightCornerLongitude, " +
                    "$lowerLeftCornerLatitude $lowerLeftCornerLongitude, " +
                    "$upperLeftCornerLatitude $upperLeftCornerLongitude))"
        } as String
    }

    private Sql getSql() { connectionProvider.sql }
}
