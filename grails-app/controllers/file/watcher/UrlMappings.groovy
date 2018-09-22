package file.watcher

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "log")

        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
