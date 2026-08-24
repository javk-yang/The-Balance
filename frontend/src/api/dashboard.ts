import request from './request'

export const dashboardApi = {
  summary: () => request.get('/dashboard/summary') as unknown as Promise<any>,

  exportExcel: (startDate?: string, endDate?: string) => {
    const params: any = {}
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    return request.get('/export/excel', {
      params,
      responseType: 'blob',
    }) as unknown as Promise<Blob>
  },
}
