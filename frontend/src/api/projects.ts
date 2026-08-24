import request from './request'
import type { Project, ProjectOverview, ProjectPayload } from '@/types'

export const projectApi = {
  list: () => request.get('/projects') as unknown as Promise<Project[]>,
  overview: () => request.get('/projects/overview') as unknown as Promise<ProjectOverview>,
  create: (data: ProjectPayload) => request.post('/projects', data) as unknown as Promise<Project>,
  update: (id: number, data: ProjectPayload) => request.put(`/projects/${id}`, data) as unknown as Promise<Project>,
  delete: (id: number) => request.delete(`/projects/${id}`),
}
