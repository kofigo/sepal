package org.openforis.sepal.component.files.query

import org.openforis.sepal.component.files.api.UserFile
import org.openforis.sepal.component.files.internal.UserDir
import org.openforis.sepal.query.Query
import org.openforis.sepal.query.QueryHandler
import org.openforis.sepal.util.annotation.ImmutableData

@ImmutableData
class ListFiles implements Query<List<UserFile>> {
    String username
    String path
}

class ListFilesHandler implements QueryHandler<List<UserFile>, ListFiles> {
    private final File homeDir

    ListFilesHandler(File homeDir) {
        this.homeDir = homeDir
    }

    List<UserFile> execute(ListFiles query) {
        return new UserDir(homeDir, query.username).list(query.path)
    }
}
